package com.v7878.dex.analysis;

import static com.v7878.dex.DexConstants.ACC_FINAL;
import static com.v7878.dex.DexConstants.ACC_STATIC;
import static com.v7878.dex.analysis.ClassLinker.LinkedClass;
import static com.v7878.dex.analysis.ClassLinker.LinkedMethod;
import static com.v7878.dex.immutable.TypeId.OBJECT;
import static com.v7878.dex.util.CollectionUtils.reverseList;

import com.v7878.dex.immutable.ClassDef;
import com.v7878.dex.immutable.FieldDef;
import com.v7878.dex.immutable.FieldId;
import com.v7878.dex.immutable.MethodDef;
import com.v7878.dex.immutable.MethodId;
import com.v7878.dex.immutable.TypeId;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Static field/method resolution and invoke dispatch — ports of the ART
 * mechanics, branch <a href="https://android.googlesource.com/platform/art/+/refs/heads/android17-release">android17-release</a>.
 * Sources and deviations are documented per element.
 *
 * <p>Hierarchy linking (vtable/iftable, copied methods) is {@link ClassLinker};
 * results are cached. Classes outside the classpath: an unknown reference-id
 * class or dispatch receiver makes the answer non-determinable
 * (Result.unknown()); the referrer affects only access checks — for an
 * unknown referrer they are computed from flags and descriptors
 * (public/private/package-private — exact answer/IAE; protected needs
 * referrer hierarchy assignability — fail-open access with exact == false).
 *
 * <p>Deviations from ART: hiddenapi is treated as allowed; IMT is skipped
 * (dispatch semantics is the itable lookup); dex-cache fast paths
 * (FindMethodFast, the NterpGetMethod cache) are not modeled — resolution
 * always takes the full path; the verifier is out of scope.
 */
public class ClassPath {

    private final Function<TypeId, ClassDef> mapper;
    private final TypeResolver resolver;
    private final ClassLinker linker;

    private final LinkedHashMap<FieldId, Result<ResolvedField>> fields_cache = new LinkedHashMap<>();
    private final LinkedHashMap<FieldCheckKey, Result<ResolvedField>> field_check_cache = new LinkedHashMap<>();
    private final LinkedHashMap<MethodId, Result<ResolvedMethod>> methods_cache = new LinkedHashMap<>();
    private final LinkedHashMap<ResolveKey, Result<LinkedMethod>> method_check_cache = new LinkedHashMap<>();
    private final LinkedHashMap<InvokeKey, Result<CallTarget>> invoke_cache = new LinkedHashMap<>();
    private final LinkedHashMap<SuperKey, Result<CallTarget>> super_cache = new LinkedHashMap<>();
    private final LinkedHashMap<NonVirtualKey, Result<CallTarget>> nonvirtual_cache = new LinkedHashMap<>();

    public ClassPath(Function<TypeId, ClassDef> mapper) {
        this.mapper = Objects.requireNonNull(mapper);
        this.resolver = TypeResolver.simple(mapper);
        this.linker = new ClassLinker(mapper);
    }

    /**
     * The library type-relation resolver — the verifier domain.
     */
    public TypeResolver getTypeResolver() {
        return resolver;
    }

    /**
     * The classpath definition for a type (null — the class is outside the classpath).
     */
    public ClassDef resolveClass(TypeId type) {
        return mapper.apply(type);
    }

    // =====================================================================
    // Result types
    // =====================================================================

    /**
     * Resolution-stage error (ResolveMethodWithChecks or the dispatch stage
     * of FindMethodToCall/FindSuperMethodToCall): the call target is not
     * found, the method is not returned. Execution-stage errors —
     * {@link InvocationError}.
     */
    public enum Error {
        NONE,
        NO_SUCH_METHOD,
        NO_SUCH_FIELD,
        /**
         * NoClassDefFoundError: the class fails to link.
         */
        NO_CLASS_DEF,
        /**
         * IllegalAccessError: CanAccess/CanAccessMember at resolution.
         */
        ILLEGAL_ACCESS,
        /**
         * IncompatibleClassChangeError: reference class mismatch / invoke kind / FindIncompatibleMethod / no dispatch.
         */
        INCOMPATIBLE_CLASS_CHANGE
    }

    /**
     * Execution-stage error — a port of ArtMethod::ThrowInvocationTimeError
     * (runtime/art_method.cc): the call target is found and returned together
     * with the error kind — see {@link CallTarget#invocationError()}.
     */
    public enum InvocationError {
        NONE,
        /**
         * AbstractMethodError.
         */
        ABSTRACT_METHOD,
        /**
         * IllegalAccessError: a non-public implementation in the receiver chain (miranda walk).
         */
        ILLEGAL_ACCESS,
        /**
         * IncompatibleClassChangeError: default-method conflict.
         */
        INCOMPATIBLE_CLASS_CHANGE
    }

    /**
     * Resolution outcome.
     *
     * @param exact   false — the hierarchy is not fully known (a class outside
     *                the classpath): the runtime would answer differently
     * @param value   the result (null on a resolution error or unknown)
     * @param error   the resolution error ART would throw
     * @param message details (classes/methods, link error text)
     */
    public record Result<T>(boolean exact, T value, Error error, String message) {

        public Result(boolean exact, T value) {
            this(exact, value, Error.NONE, null);
        }

        public static <T> Result<T> ok(boolean exact, T value) {
            return new Result<>(exact, value, Error.NONE, null);
        }

        public static <T> Result<T> fail(boolean exact, Error error, String message) {
            return new Result<>(exact, null, error, message);
        }

        /**
         * The answer is non-determinable — the runtime knows more than the
         * model: the reference-id class, the dispatch receiver, or (the class
         * branch of invoke-super) the referrer superclass is unknown.
         */
        public static <T> Result<T> unknown() {
            return new Result<>(false, null, Error.NONE, null);
        }
    }

    /**
     * A resolved field: the id is mapped to its def.
     */
    public record ResolvedField(ClassDef declaringClass, FieldDef field) {
        public ResolvedField {
            Objects.requireNonNull(declaringClass);
            Objects.requireNonNull(field);
        }
    }

    /**
     * A (class, method) pair: the method id is mapped to its def.
     */
    public record ResolvedMethod(ClassDef declaringClass, MethodDef method) {
        public ResolvedMethod {
            Objects.requireNonNull(declaringClass);
            Objects.requireNonNull(method);
        }

        public boolean isDefault() {
            return declaringClass.isInterface() && method.getImplementation() != null;
        }
    }

    /**
     * Invoke instruction kind — a port of {@code InvokeType}
     * (libdexfile/dex/invoke_type.h); kPolymorphic/kCustom are out of scope.
     * STATIC/DIRECT are served by {@code findNonVirtualMethodToCall},
     * VIRTUAL/INTERFACE — by {@code findMethodToCall}, SUPER — by
     * {@code findSuperMethodToCall}.
     */
    public enum InvokeType {STATIC, DIRECT, VIRTUAL, SUPER, INTERFACE}

    /**
     * Field access kind — the iget/iput/sget/sput instructions (ART: the
     * StaticBit/WriteBit of the FindFieldFromCode type). Serves
     * {@code resolveField(fid, referrer, access)}.
     */
    public enum FieldAccess {
        IGET, IPUT, SGET, SPUT;

        boolean isStatic() {
            return this == SGET || this == SPUT;
        }

        boolean isPut() {
            return this == IPUT || this == SPUT;
        }
    }

    /**
     * Kind of a methods-array entry vs the ART flags (kAccCopied — "copied
     * from an interface"): DECLARED — a method declared by the class;
     * MIRANDA — a miranda copy (kAccCopied | kAccAbstract); DEFAULT — a
     * default-method copy (kAccCopied | kAccDefault); DEFAULT_CONFLICT — a
     * conflict copy (kAccCopied | kAccAbstract | kAccDefault, not invokable).
     */
    public enum MethodKind {DECLARED, MIRANDA, DEFAULT, DEFAULT_CONFLICT}

    /**
     * Dispatch outcome: a concrete method or a default-method conflict. The
     * execution error lives in the target itself — {@link #invocationError()}.
     */
    public sealed interface CallTarget {
        InvocationError invocationError();

        /**
         * A single method: the resolved method, entry kind and execution
         * error; {@link #isInvokable()} is derived
         * ({@code invocationError == NONE}).
         */
        record Method(ResolvedMethod method, MethodKind kind,
                      InvocationError invocationError) implements CallTarget {
            public Method {
                Objects.requireNonNull(method);
                Objects.requireNonNull(kind);
                Objects.requireNonNull(invocationError);
            }

            public boolean isInvokable() {
                return invocationError == InvocationError.NONE;
            }
        }

        /**
         * A default-method conflict (ART: a stub method with
         * EnsureThrowsInvocationError): execution would throw ICCE; the
         * candidates are all unmasked default methods.
         */
        record DefaultConflict(List<ResolvedMethod> candidates) implements CallTarget {
            public DefaultConflict {
                Objects.requireNonNull(candidates);
                candidates = List.copyOf(candidates);
            }

            @Override
            public InvocationError invocationError() {
                return InvocationError.INCOMPATIBLE_CLASS_CHANGE;
            }
        }
    }

    private record ResolveKey(MethodId mid, TypeId referrer, InvokeType type) {
    }

    private record InvokeKey(MethodId mid, TypeId referrer, InvokeType type, TypeId receiver) {
    }

    private record SuperKey(MethodId mid, TypeId referrer, TypeId receiver) {
    }

    private record NonVirtualKey(MethodId mid, TypeId referrer, InvokeType type) {
    }

    private record FieldCheckKey(FieldId fid, TypeId referrer, FieldAccess access) {
    }

    // =====================================================================
    // Field resolution
    // =====================================================================

    /**
     * Resolves a field by FieldId (cached). An array declaring class is
     * normalized to j.l.Object — array fields are Object fields. Source and
     * deviations — {@link #resolveFieldUncached}.
     */
    public Result<ResolvedField> resolveField(FieldId fid) {
        Objects.requireNonNull(fid);
        fid = normalize(fid);
        return fields_cache.computeIfAbsent(fid, this::resolveFieldUncached);
    }

    /**
     * A port of ClassLinker::ResolveFieldJLS → Class::FindField
     * (FindFieldImpl, runtime/mirror/class.cc): the JLS order — the class
     * itself → its direct interfaces (static fields only, recursively) → the
     * superclass (and its interfaces), up the chain. An iterative deque walk,
     * cycle-safe. No access or kind checks — for the checking path see
     * {@link #resolveField(FieldId, TypeId, FieldAccess)}.
     */
    private Result<ResolvedField> resolveFieldUncached(FieldId fid) {
        var startType = fid.getDeclaringClass();
        var startLC = linker.link(startType);
        if (startLC.linkError != null) {
            return Result.fail(true, Error.NO_CLASS_DEF,
                    "class failed to link: " + startLC.linkError);
        }
        // exactness is computed by the traversal: a hit before an unknown
        // node is exact (the unknown part lies further in the search order
        // and cannot shadow it); an unknown node makes all later answers
        // inexact
        boolean exact = true;
        var done = new LinkedHashSet<TypeId>();
        var todo = new ArrayDeque<TypeId>();
        todo.add(startType);
        while (!todo.isEmpty()) {
            var t = todo.poll();
            if (!done.add(t)) {
                continue;
            }
            var def = resolveClass(t);
            if (def == null) {
                exact = false;
                continue;
            }
            // interfaces are searched for static fields only (FindStaticField);
            // an interface superclass is not visited: always j.l.Object, no fields
            var field = def.isInterface()
                    ? def.findStaticField(fid.getName(), fid.getType())
                    : def.findField(fid.getName(), fid.getType());
            if (field != null) {
                return Result.ok(exact, new ResolvedField(def, field));
            }
            if (!def.isInterface()) {
                var s = def.getSuperclass();
                if (s != null) {
                    todo.addFirst(s);
                }
            }
            for (var iface : reverseList(def.getInterfaces())) {
                todo.addFirst(iface);
            }
            // the queue head is now {I1, ..., IN, SUPER, ...}
        }
        return Result.fail(exact, Error.NO_SUCH_FIELD, null);
    }

    /**
     * A port of ResolveFieldWithAccessChecks
     * (runtime/entrypoints/entrypoint_utils-inl.h) for iget/sget/iput/sput.
     * {@code referrer} — the class containing the access; {@code access} —
     * the instruction kind (read/write × instance/static).
     *
     * <p>Order as in ART: ResolveFieldJLS (NoSuchFieldError on miss) → field
     * kind (IsStatic != isStatic → ICCE, before access checks — the reverse
     * of the method order) → ResolvedFieldAccessTest (CanAccess(declarer)
     * with an escape hatch to the FieldId class → IAE; CanAccessMember(flags)
     * → IAE) → CanBeChangedBy (a write to a final field of another declarer
     * → IAE).
     *
     * <p>An unknown referrer does not make the result unknown: CanBeChangedBy
     * and the public/private/package-private branches depend only on
     * descriptors and flags — exact answer/IAE; the indeterminate protected
     * branch (referrer hierarchy assignability) and CanAccess on an unknown
     * target class — fail-open access with exact=false.
     */
    public Result<ResolvedField> resolveField(FieldId fid, TypeId referrer, FieldAccess access) {
        Objects.requireNonNull(fid);
        Objects.requireNonNull(referrer);
        Objects.requireNonNull(access);
        fid = normalize(fid);
        return field_check_cache.computeIfAbsent(new FieldCheckKey(fid, referrer, access),
                key -> resolveFieldWithChecksUncached(key.fid(), key.referrer, key.access));
    }

    private Result<ResolvedField> resolveFieldWithChecksUncached(FieldId fid, TypeId referrer, FieldAccess access) {
        var refLC = linker.link(referrer);
        if (refLC.linkError != null) {
            return Result.fail(true, Error.NO_CLASS_DEF,
                    "referrer failed to link: " + refLC.linkError);
        }
        var r = resolveField(fid);
        if (r.value() == null) {
            return Result.fail(r.exact(), r.error(), r.message());
        }
        var resolved = r.value();
        var exact = r.exact();
        boolean fieldIsStatic = (resolved.field().getAccessFlags() & ACC_STATIC) != 0;
        if (fieldIsStatic != access.isStatic()) {
            return Result.fail(exact, Error.INCOMPATIBLE_CLASS_CHANGE,
                    "expected " + (access.isStatic() ? "static" : "instance") + " field, got "
                            + (fieldIsStatic ? "static" : "instance") + ": " + resolved);
        }
        var declaringLC = linker.link(resolved.declaringClass().getType());
        var klassLC = linker.link(fid.getDeclaringClass());
        // IAE only when both are definitely denied; indeterminate (unknown
        // target class) — fail-open with exact=false
        var accessDeclaring = linker.canAccess(referrer, declaringLC);
        var accessKlass = linker.canAccess(referrer, klassLC);
        if (!Boolean.TRUE.equals(accessDeclaring) && !Boolean.TRUE.equals(accessKlass)) {
            if (accessDeclaring == null || accessKlass == null) {
                exact = false;
            } else {
                return Result.fail(exact, Error.ILLEGAL_ACCESS,
                        "class " + referrer + " cannot access class " + klassLC.type
                                + " for field access: " + resolved);
            }
        }
        var memberAccess = linker.canAccessMember(
                refLC, declaringLC, resolved.field().getAccessFlags());
        if (memberAccess == null) {
            // protected with an unknown/incomplete referrer hierarchy — fail-open
            exact = false;
        } else if (!memberAccess) {
            return Result.fail(exact, Error.ILLEGAL_ACCESS,
                    "class " + referrer + " cannot access field " + resolved);
        }
        if (access.isPut() && !canBeChangedBy(resolved, referrer)) {
            return Result.fail(exact, Error.ILLEGAL_ACCESS,
                    "final field of " + resolved.declaringClass().getType()
                            + " cannot be written from " + referrer + ": " + resolved);
        }
        return Result.ok(exact, resolved);
    }

    /**
     * A port of ArtField::CanBeChangedBy (runtime/art_field-inl.h): a write
     * is allowed if the field is not final or the declarer is the accessing
     * class itself (own final fields; package and protected do not apply, no
     * constructor branches — ART runtime leniency). For an unknown referrer
     * the declarer of a known field is always another class (descriptors
     * differ) — the answer is deterministic.
     */
    private static boolean canBeChangedBy(ResolvedField resolved, TypeId referrer) {
        return (resolved.field().getAccessFlags() & ACC_FINAL) == 0
                || resolved.declaringClass().getType().equals(referrer);
    }

    // =====================================================================
    // Method resolution
    // =====================================================================

    /**
     * A port of ClassLinker::ResolveMethodId → FindResolvedMethod:
     * resolution without access checks (the SkipAccessChecks path of
     * NterpGetMethod — verified code). An array reference class is normalized
     * to j.l.Object.
     */
    public Result<ResolvedMethod> resolveMethod(MethodId mid) {
        Objects.requireNonNull(mid);
        mid = normalize(mid);
        return methods_cache.computeIfAbsent(mid, this::resolveMethodUncached);
    }

    private Result<ResolvedMethod> resolveMethodUncached(MethodId mid) {
        var r = resolveLinkedMethod(mid);
        if (r.value() == null) {
            return Result.fail(r.exact(), r.error(), r.message());
        }
        return Result.ok(r.exact(), toResolved(r.value()));
    }

    private Result<LinkedMethod> resolveLinkedMethod(MethodId mid) {
        var klassType = mid.getDeclaringClass();
        if (klassType.isArray()) {
            mid = MethodId.of(OBJECT, mid.getName(), mid.getProto());
            klassType = OBJECT;
        }
        var lc = linker.link(klassType);
        if (lc.linkError != null) {
            return Result.fail(true, Error.NO_CLASS_DEF,
                    "class failed to link: " + lc.linkError);
        }
        if (lc.isUnknown()) {
            return Result.unknown();
        }
        // exactness is computed by the search: hits in the known part are
        // exact; degradation — only where the answer depends on the invisible
        // hierarchy
        var exact = new boolean[]{true};
        var found = lc.isInterface()
                ? linker.findInterfaceMethodWithSignature(lc, mid.getName(), mid.getProto(), exact)
                : linker.findClassMethod(lc, mid.getName(), mid.getProto(), exact);
        if (found == null) {
            return Result.fail(exact[0], Error.NO_SUCH_METHOD, null);
        }
        return Result.ok(exact[0], found);
    }

    /**
     * A port of ClassLinker::ResolveMethodWithChecks
     * (runtime/class_linker-inl.h) — the full checking path.
     * {@code referrer} — the class containing the call; {@code type} — the
     * invoke instruction kind.
     *
     * <p>Order: ThrowIfInvokeClassMismatch (ICCE before the search; only
     * VIRTUAL/INTERFACE are checked — SUPER/STATIC are not, DIRECT is legal
     * on modern-dex interfaces) → search (FindClassMethod /
     * FindInterfaceMethodWithSignature) → CanAccess(declarer class) with the
     * CanAccess(MethodId reference class) escape hatch (IAE) → CanAccessMember
     * (IAE) → CheckIncompatibleClassChange (ICCE) → FindIncompatibleMethod
     * (ICCE) → NoSuchMethodError.
     *
     * <p>An unknown referrer does not make the result unknown: it affects
     * only the access checks — the public/private/package-private branches of
     * CanAccessMember depend only on descriptors and flags — exact
     * answer/IAE; the indeterminate protected branch (referrer hierarchy
     * assignability) and CanAccess on an unknown target class — fail-open
     * access with exact=false.
     */
    public Result<ResolvedMethod> resolveMethod(MethodId mid, TypeId referrer, InvokeType type) {
        Objects.requireNonNull(mid);
        Objects.requireNonNull(referrer);
        Objects.requireNonNull(type);
        var m = normalize(mid);
        var r = resolveMethodWithChecksCached(m, referrer, type);
        if (r.value() == null) {
            return Result.fail(r.exact(), r.error(), r.message());
        }
        return Result.ok(r.exact(), toResolved(r.value()));
    }

    private Result<LinkedMethod> resolveMethodWithChecksCached(
            MethodId mid, TypeId referrer, InvokeType type) {
        return method_check_cache.computeIfAbsent(new ResolveKey(mid, referrer, type),
                key -> resolveMethodWithChecks(key.mid(), key.referrer(), key.type()));
    }

    private Result<LinkedMethod> resolveMethodWithChecks(
            MethodId mid, TypeId referrer, InvokeType type) {
        var klassType = mid.getDeclaringClass();
        var lc = linker.link(klassType);
        if (lc.linkError != null) {
            return Result.fail(true, Error.NO_CLASS_DEF,
                    "class failed to link: " + lc.linkError);
        }
        if (lc.isUnknown()) {
            return Result.unknown();
        }
        var refLC = linker.link(referrer);
        if (refLC.linkError != null) {
            return Result.fail(true, Error.NO_CLASS_DEF,
                    "referrer failed to link: " + refLC.linkError);
        }
        if (type == InvokeType.INTERFACE && !lc.isInterface()) {
            return Result.fail(true, Error.INCOMPATIBLE_CLASS_CHANGE,
                    "Found class " + klassType + ", but interface was expected");
        }
        if (type == InvokeType.VIRTUAL && lc.isInterface()) {
            return Result.fail(true, Error.INCOMPATIBLE_CLASS_CHANGE,
                    "Found interface " + klassType + ", but class was expected");
        }
        // the search computes the klass hierarchy completeness; the referrer
        // affects only the access checks below — they degrade exactness in place
        var exact = new boolean[]{true};
        var resolved = lc.isInterface()
                ? linker.findInterfaceMethodWithSignature(lc, mid.getName(), mid.getProto(), exact)
                : linker.findClassMethod(lc, mid.getName(), mid.getProto(), exact);
        if (resolved == null) {
            // FindIncompatibleMethod: the retry is interface-only (FindClassMethod
            // sees static); ART returns null for classes
            if (lc.isInterface()) {
                resolved = linker.findClassMethod(lc, mid.getName(), mid.getProto(), exact);
                if (resolved != null) {
                    return Result.fail(exact[0], Error.INCOMPATIBLE_CLASS_CHANGE,
                            "incompatible method: " + resolved);
                }
            }
            return Result.fail(exact[0], Error.NO_SUCH_METHOD,
                    "no method " + mid.getName() + mid.getProto() + " in " + klassType);
        }
        // CanAccess(declarer) with the MethodId class escape hatch (the method
        // may be accessible via a public subclass of the declarer)
        // IAE only when both are definitely denied; indeterminate (unknown
        // target class) — fail-open with exact=false
        var methodsClass = resolved.declaring;
        var accessDeclaring = linker.canAccess(referrer, methodsClass);
        var accessKlass = linker.canAccess(referrer, lc);
        if (!Boolean.TRUE.equals(accessDeclaring) && !Boolean.TRUE.equals(accessKlass)) {
            if (accessDeclaring == null || accessKlass == null) {
                exact[0] = false;
            } else {
                return Result.fail(exact[0], Error.ILLEGAL_ACCESS,
                        "class " + referrer + " cannot access class " + lc.type
                                + " for method dispatch: " + resolved);
            }
        }
        var memberAccess = linker.canAccessMember(
                refLC, methodsClass, resolved.def.getAccessFlags());
        if (memberAccess == null) {
            exact[0] = false;
        } else if (!memberAccess) {
            return Result.fail(exact[0], Error.ILLEGAL_ACCESS,
                    "class " + referrer + " cannot access method " + resolved);
        }
        // CheckIncompatibleClassChange
        if (checkIncompatibleClassChange(resolved, type)) {
            return Result.fail(exact[0], Error.INCOMPATIBLE_CLASS_CHANGE,
                    "invoke-" + type + " on incompatible method: " + resolved);
        }
        return Result.ok(exact[0], resolved);
    }

    // =====================================================================
    // Dispatch: FindMethodToCall / FindSuperMethodToCall / non-virtual
    // =====================================================================

    /**
     * A port of FindMethodToCall&lt;type&gt;
     * (runtime/entrypoints/entrypoint_utils-inl.h) for invoke-virtual /
     * invoke-interface (resolution — ResolveMethodWithChecks from the
     * referrer). {@code receiver} — the receiver class (the runtime analog
     * of this.getClass()); {@code referrer} — the class containing the call.
     *
     * <p>Dispatch: VIRTUAL — the receiver vtable at the resolved method's
     * index; INTERFACE — the itable (FindVirtualMethodForInterface), except a
     * j.l.Object method — the vtable (kInvokeInterfaceOnObjectMethodFlag in
     * NterpGetMethod). An array receiver dispatches as j.l.Object (the array
     * vtable = the Object vtable, Cloneable/Serializable declare no methods).
     * A missing itable dispatch — ICCE
     * (ThrowIncompatibleClassChangeErrorClassForInterfaceDispatch). The target
     * may be non-invokable — see {@link CallTarget#invocationError()}; a
     * default-method conflict — {@link CallTarget.DefaultConflict}.
     */
    public Result<CallTarget> findMethodToCall(MethodId mid, TypeId referrer,
                                               InvokeType type, TypeId receiver) {
        Objects.requireNonNull(mid);
        Objects.requireNonNull(receiver);
        Objects.requireNonNull(type);
        Objects.requireNonNull(referrer);
        if (type != InvokeType.VIRTUAL && type != InvokeType.INTERFACE) {
            throw new IllegalArgumentException("findMethodToCall: " + type
                    + " (static/direct -> findNonVirtualMethodToCall, super -> findSuperMethodToCall)");
        }
        var m = normalize(mid);
        var r = receiver.isArray() ? OBJECT : receiver;
        return invoke_cache.computeIfAbsent(new InvokeKey(m, referrer, type, r),
                key -> findMethodToCallUncached(key.mid(), key.referrer(), key.type(), key.receiver()));
    }

    private Result<CallTarget> findMethodToCallUncached(
            MethodId mid, TypeId referrer, InvokeType type, TypeId receiver) {
        var resolution = resolveMethodWithChecksCached(mid, referrer, type);
        if (resolution.value() == null) {
            return Result.fail(resolution.exact(), resolution.error(), resolution.message());
        }
        var resolved = resolution.value();
        var rlc = linker.link(receiver);
        if (rlc.linkError != null) {
            return Result.fail(true, Error.NO_CLASS_DEF,
                    "receiver failed to link: " + rlc.linkError);
        }
        if (rlc.isUnknown()) {
            return Result.unknown();
        }
        if (rlc.isInterface()) {
            return Result.fail(resolution.exact(), Error.INCOMPATIBLE_CLASS_CHANGE,
                    "receiver is an interface: " + receiver);
        }
        var exact = resolution.exact() && rlc.exact;
        var called = switch (type) {
            case VIRTUAL -> vtableEntry(rlc, resolved.methodIndex);
            case INTERFACE -> {
                if (resolved.declaring.type.equals(OBJECT)) {
                    yield vtableEntry(rlc, resolved.methodIndex);
                }
                yield linker.findVirtualMethodForInterface(resolved, rlc);
            }
            default -> throw new IllegalStateException(type.toString());
        };
        if (called == null) {
            // ThrowIncompatibleClassChangeErrorClassForInterfaceDispatch
            return Result.fail(exact, Error.INCOMPATIBLE_CLASS_CHANGE,
                    "no dispatch for " + resolved + " in receiver " + receiver);
        }
        return wrap(called, exact, receiver);
    }

    /**
     * A port of FindSuperMethodToCall&lt;access_check=true&gt;
     * (runtime/entrypoints/entrypoint_utils-inl.h) for dex invoke-super
     * (resolution — ResolveMethodWithChecks(kSuper)). {@code referrer} — the
     * class containing the call; {@code receiver} — the receiver class (this):
     * it does not affect the dispatch, it is needed only for the execution
     * error (the miranda walk goes along the receiver chain).
     *
     * <p>Class branch: the referrer superclass vtable at the resolved
     * method's vtable index; interface branch —
     * FindVirtualMethodForInterfaceSuper. A referrer outside the instruction
     * class hierarchy, the referrer being j.l.Object itself (no superclass),
     * or an index outside the superclass vtable —
     * NoSuchMethodError (the class in the message is the resolved method's
     * declarer, as in ART). Unknown referrer: assignability (the inheritance
     * gate) is non-determinable — fail-open with exact=false; the interface
     * branch does not depend on the referrer (result with exact=false), the
     * class branch needs the referrer superclass — Result.unknown() (the only
     * referrer-caused unknown). An unknown superclass of a known referrer —
     * an inexact NSME: the real superclass could dispatch the index.
     */
    public Result<CallTarget> findSuperMethodToCall(
            MethodId mid, TypeId referrer, TypeId receiver) {
        Objects.requireNonNull(mid);
        Objects.requireNonNull(referrer);
        Objects.requireNonNull(receiver);
        var m = normalize(mid);
        var r = receiver.isArray() ? OBJECT : receiver;
        return super_cache.computeIfAbsent(new SuperKey(m, referrer, r),
                key -> findSuperMethodToCallUncached(key.mid(), key.referrer(), key.receiver()));
    }

    private Result<CallTarget> findSuperMethodToCallUncached(
            MethodId mid, TypeId referrer, TypeId receiver) {
        // NterpGetMethod: kSuper -> ResolveMethodWithChecks, then FindSuperMethodToCall
        var resolution = resolveMethodWithChecksCached(mid, referrer, InvokeType.SUPER);
        if (resolution.value() == null) {
            return Result.fail(resolution.exact(), resolution.error(), resolution.message());
        }
        var resolved = resolution.value();
        var lc = linker.link(mid.getDeclaringClass());
        var refLC = linker.link(referrer);
        var exact = new boolean[]{resolution.exact()};
        var assignable = linker.isAssignableFrom(lc, refLC);
        if (assignable == null) {
            exact[0] = false;
        } else if (!assignable) {
            return Result.fail(exact[0], Error.NO_SUCH_METHOD,
                    "no super method: " + referrer +
                            " is not assignable to " + mid.getDeclaringClass());
        }
        if (lc.isInterface()) {
            if (!resolved.declaring.isInterface()) {
                // invoke-super from interface should not resolve to Object methods
                return Result.fail(exact[0], Error.INCOMPATIBLE_CLASS_CHANGE,
                        "invoke-super from interface resolved to Object method: " + resolved);
            }
            var found = linker.findVirtualMethodForInterfaceSuper(lc, resolved, exact);
            if (found == null) {
                return Result.fail(exact[0], Error.NO_SUCH_METHOD,
                        "no super method " + resolved);
            }
            return wrap(found, exact[0], receiver);
        }
        if (refLC.isUnknown()) {
            return Result.unknown();
        }
        var superLC = refLC.superLC;
        int vi = resolved.methodIndex;
        if (superLC != null && superLC.isUnknown()) {
            exact[0] = false;
        }
        if (superLC == null || superLC.isUnknown() || vi < 0 || vi >= superLC.vtable.size()) {
            return Result.fail(exact[0], Error.NO_SUCH_METHOD,
                    "no super method " + resolved + " in class " + resolved.declaring.type);
        }
        return wrap(superLC.vtable.get(vi), exact[0], receiver);
    }

    /**
     * invoke-static / invoke-direct (resolution — ResolveMethodWithChecks).
     * No receiver: absent in a static call, irrelevant in a direct one; no
     * dispatch — the call target is the resolved method itself (NterpGetMethod
     * caches it as is). Kind checks — CheckIncompatibleClassChange: kStatic —
     * !IsStatic; kDirect — !IsDirect || IsStatic. The execution error is
     * computed with receiver == null (like ThrowInvocationTimeError(nullptr)
     * in ART: no miranda walk).
     */
    public Result<CallTarget> findNonVirtualMethodToCall(
            MethodId mid, TypeId referrer, InvokeType type) {
        Objects.requireNonNull(mid);
        Objects.requireNonNull(type);
        Objects.requireNonNull(referrer);
        if (type != InvokeType.STATIC && type != InvokeType.DIRECT) {
            throw new IllegalArgumentException("findNonVirtualMethodToCall: " + type);
        }
        var m = normalize(mid);
        return nonvirtual_cache.computeIfAbsent(new NonVirtualKey(m, referrer, type),
                key -> findNonVirtualMethodToCallUncached(key.mid(), key.referrer(), key.type()));
    }

    private Result<CallTarget> findNonVirtualMethodToCallUncached(
            MethodId mid, TypeId referrer, InvokeType type) {
        var resolution = resolveMethodWithChecksCached(mid, referrer, type);
        if (resolution.value() == null) {
            return Result.fail(resolution.exact(), resolution.error(), resolution.message());
        }
        return wrap(resolution.value(), resolution.exact(), null);
    }

    // =====================================================================
    // Helpers
    // =====================================================================

    private static MethodId normalize(MethodId mid) {
        var klassType = mid.getDeclaringClass();
        if (klassType.isArray()) {
            return MethodId.of(OBJECT, mid.getName(), mid.getProto());
        }
        return mid;
    }

    private static FieldId normalize(FieldId fid) {
        var klassType = fid.getDeclaringClass();
        if (klassType.isArray()) {
            return FieldId.of(OBJECT, fid.getName(), fid.getType());
        }
        return fid;
    }

    private static LinkedMethod vtableEntry(LinkedClass lc, int index) {
        if (index < 0 || index >= lc.vtable.size()) {
            return null;
        }
        return lc.vtable.get(index);
    }

    /**
     * A port of ArtMethod::CheckIncompatibleClassChange (art_method-inl.h)
     * for every modeled invoke kind (kPolymorphic out of scope).
     */
    private static boolean checkIncompatibleClassChange(LinkedMethod m, InvokeType type) {
        return switch (type) {
            case STATIC -> !m.isStatic();
            case DIRECT -> !m.isDirect() || m.isStatic();
            case VIRTUAL -> m.isDirect()
                    || (m.declaring.isInterface() && m.kind == MethodKind.DECLARED);
            case SUPER -> m.isConstructor() || m.isStatic();
            case INTERFACE -> m.isDirect()
                    || !(m.declaring.isInterface() || m.declaring.type.equals(OBJECT));
        };
    }

    private static ResolvedMethod toResolved(LinkedMethod m) {
        return new ResolvedMethod(m.declaring.def, m.def);
    }

    /**
     * Wraps the dispatch result: execution errors live in the target itself
     * (a port of ArtMethod::ThrowInvocationTimeError — the method is found
     * and returned together with the error kind). If the receiver class is
     * unknown, fails to link, or its hierarchy is incomplete (invisible
     * declared methods may remain in the chain), the execution error kind is
     * non-determinable — the result is marked exact=false.
     */
    private Result<CallTarget> wrap(LinkedMethod called, boolean exact, TypeId receiverType) {
        var kind = called.kind;
        if (kind == MethodKind.DEFAULT_CONFLICT) {
            var candidates = new ArrayList<ResolvedMethod>(called.conflictCandidates.size());
            for (var lm : called.conflictCandidates) {
                candidates.add(toResolved(lm));
            }
            return Result.ok(exact, new CallTarget.DefaultConflict(List.copyOf(candidates)));
        }
        var invokable = called.isInvokable();
        var invocationError = InvocationError.NONE;
        if (!invokable) {
            var receiverLC = receiverType == null ? null : linker.link(receiverType);
            if (receiverLC != null &&
                    (receiverLC.isUnknown() || receiverLC.linkError != null || !receiverLC.exact)) {
                exact = false;
                receiverLC = null;
            }
            invocationError = linker.throwInvocationTimeError(called, receiverLC);
        }

        assert invokable == (invocationError == InvocationError.NONE);
        return Result.ok(exact, new CallTarget.Method(toResolved(called), kind, invocationError));
    }
}
