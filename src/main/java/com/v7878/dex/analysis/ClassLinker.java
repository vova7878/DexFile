package com.v7878.dex.analysis;

import static com.v7878.dex.DexConstants.ACC_ABSTRACT;
import static com.v7878.dex.DexConstants.ACC_FINAL;
import static com.v7878.dex.DexConstants.ACC_PRIVATE;
import static com.v7878.dex.DexConstants.ACC_PROTECTED;
import static com.v7878.dex.DexConstants.ACC_PUBLIC;
import static com.v7878.dex.DexConstants.ACC_STATIC;
import static com.v7878.dex.DexConstants.NO_INDEX;

import com.v7878.collections.IntMap;
import com.v7878.dex.analysis.ClassPath.InvocationError;
import com.v7878.dex.analysis.ClassPath.MethodKind;
import com.v7878.dex.immutable.ClassDef;
import com.v7878.dex.immutable.MethodDef;
import com.v7878.dex.immutable.ProtoId;
import com.v7878.dex.immutable.TypeId;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

/**
 * Static class-linking model — a port of ClassLinker/LinkMethodsHelper
 * (runtime/class_linker.cc), branch <a href="https://android.googlesource.com/platform/art/+/refs/heads/android17-release">android17-release</a>.
 * Builds, per class, the methods array ([direct | declared virtual | copied]),
 * vtable, iftable (with implementation method arrays), and the kind of copied
 * entries (miranda / default copy / default conflict). Sources and deviations
 * are documented per element.
 *
 * <p>Invariants:
 * <ul>
 *   <li>iterative building (explicit stacks/deques); hierarchy cycles (a
 *       broken dex) do not hang but become a class link error;</li>
 *   <li>vtable indexes are self-consistent: with an unknown (outside the
 *       classpath) superclass, numbering is relative to the known part (the
 *       prefix is assumed length 0) — the "resolved index → receiver vtable"
 *       dispatch stays correct, the class is marked exact=false;</li>
 *   <li>link errors (final-override, implements non-interface, cycles, too
 *       many methods) are reported as linkError and propagated to subclasses
 *       (in ART the class fails to load, any use of it —
 *       NoClassDefFoundError).</li>
 * </ul>
 */
final class ClassLinker {

    private final LinkedHashMap<TypeId, LinkedClass> cache = new LinkedHashMap<>();
    private final Function<TypeId, ClassDef> mapper;
    private final LinkedClass OBJ;

    ClassLinker(Function<TypeId, ClassDef> mapper) {
        this.mapper = Objects.requireNonNull(mapper);
        OBJ = link(TypeId.OBJECT);
    }

    private ClassDef resolveClass(TypeId type) {
        return mapper.apply(type);
    }

    // =====================================================================
    // Model
    // =====================================================================

    /**
     * A method in some class's methods array. For copies, declaring is the
     * source interface (ART ArtMethod::CopyFrom keeps the declaring class);
     * for declared methods — the class itself. The (declaring.def, def) pair
     * is exactly a ResolvedMethod: the id is mapped to its def.
     */
    static final class LinkedMethod {
        final LinkedClass declaring;
        final MethodDef def;
        final MethodKind kind;
        int methodIndex = -1;  // vtable index (classes) / index among interface virtuals
        List<LinkedMethod> conflictCandidates = List.of(); // DEFAULT_CONFLICT only

        LinkedMethod(LinkedClass declaring, MethodDef def, MethodKind kind) {
            this.declaring = declaring;
            this.def = def;
            this.kind = kind;
        }

        boolean isStatic() {
            return (def.getAccessFlags() & ACC_STATIC) != 0;
        }

        boolean isFinal() {
            return (def.getAccessFlags() & ACC_FINAL) != 0;
        }

        boolean isPublic() {
            return (def.getAccessFlags() & ACC_PUBLIC) != 0;
        }

        boolean isDirect() {
            return kind == MethodKind.DECLARED && def.isDirect();
        }

        boolean isVirtual() {
            return !isDirect();
        }

        boolean isConstructor() {
            return def.isConstructor();
        }

        @SuppressWarnings("unused")
        boolean isMiranda() {
            return kind == MethodKind.MIRANDA;
        }

        /**
         * A port of ArtMethod::IsDefault (kAccDefault, runtime/art_method.h):
         * an interface virtual with a body. Derived from declaring/kind/flags
         * — DEFAULT/DEFAULT_CONFLICT copies always have such a source, a
         * miranda source is abstract.
         */
        boolean isDefault() {
            return declaring.isInterface() && isVirtual()
                    && (def.getAccessFlags() & ACC_ABSTRACT) == 0;
        }

        boolean isDefaultConflicting() {
            return kind == MethodKind.DEFAULT_CONFLICT;
        }

        /**
         * The ACC_ABSTRACT flag of the dex definition — equivalent to
         * ArtMethod::IsAbstract (runtime/art_method.h). Model deviation: def
         * carries the original flags (no runtime kAccCopied/kAccDefault — the
         * entry kind lives in kind), while in ART default-conflict copies
         * carry kAccAbstract as a marker and are excluded by the
         * IsDefaultConflicting check. The kind correspondence — assert.
         */
        boolean isAbstract() {
            var is_abstract = (def.getAccessFlags() & ACC_ABSTRACT) != 0;
            assert kind != MethodKind.MIRANDA || is_abstract;
            assert kind != MethodKind.DEFAULT_CONFLICT || !is_abstract;
            return is_abstract;
        }

        /**
         * A port of ArtMethod::IsInvokable: not abstract and not a conflict
         * stub (miranda is covered by its abstract source).
         */
        boolean isInvokable() {
            return !isDefaultConflicting() && !isAbstract();
        }

        /**
         * ArtMethod::IsOverridableByDefaultMethod: declaring is an interface
         * (any copy or a declared interface method).
         */
        boolean isOverridableByDefaultMethod() {
            return kind != MethodKind.DECLARED || declaring.isInterface();
        }

        String name() {
            return def.getName();
        }

        ProtoId proto() {
            return def.getProto();
        }

        boolean matches(String name, ProtoId proto) {
            return name().equals(name) && proto().equals(proto);
        }

        @Override
        public String toString() {
            return declaring.type + "." + name() + proto();
        }
    }

    /**
     * An iftable entry: an interface + the implementation method array
     * (index = the interface index of a virtual).
     *
     * @param methodArray null — the interface has no declared virtuals
     */
    record IfTableEntry(LinkedClass iface, List<LinkedMethod> methodArray) {
    }

    static final class LinkedClass {
        final TypeId type;
        final ClassDef def;    // null → the class is unknown to the classpath (isUnknown())
        boolean exact = true;
        boolean hasDefaultMethods; // interfaces: kAccHasDefaultMethod
        String linkError;          // != null → the class fails to link (ART: load error)
        LinkedClass superLC;       // superclass; null — Object and unknown classes
        List<LinkedMethod> declaredDirect = List.of();
        List<LinkedMethod> declaredVirtual = List.of();
        List<LinkedMethod> copied = List.of();
        // USE_SUPER aliases: present in the vtable but not in the methods
        // array — invisible to FindClassMethod (no kUseSuperMethod copy in
        // the class, t13). Deviation from ART: it reuses the superclass
        // method object itself; here — a separate alias with the same
        // def/declaring/kind/index
        List<LinkedMethod> aliases = List.of();
        List<LinkedMethod> vtable = List.of(); // classes; unused for interfaces
        List<IfTableEntry> iftable = List.of();

        LinkedClass(TypeId type, ClassDef def) {
            this.type = type;
            this.def = def;
        }

        /**
         * A port of Class::IsInterface (the class kAccInterface). An unknown
         * class (def == null) is not an interface.
         */
        boolean isInterface() {
            return def != null && def.isInterface();
        }

        /**
         * A class outside the classpath (def == null): linking is impossible,
         * the hierarchy is entirely unknown. Derived from the final def — a
         * desync with exact is structurally impossible; invariant:
         * isUnknown() ⇒ !exact (the converse does not hold: a known
         * hierarchy may be incomplete).
         */
        boolean isUnknown() {
            return def == null;
        }

        List<LinkedMethod> allDeclared() {
            var out = new ArrayList<LinkedMethod>(declaredDirect.size() + declaredVirtual.size());
            out.addAll(declaredDirect);
            out.addAll(declaredVirtual);
            return out;
        }

        List<LinkedMethod> allMethods() {
            var out = allDeclared();
            out.addAll(copied);
            return out;
        }

        List<LinkedMethod> allVirtuals() {
            var out = new ArrayList<LinkedMethod>(declaredVirtual.size() + copied.size() + aliases.size());
            out.addAll(declaredVirtual);
            out.addAll(copied);
            out.addAll(aliases);
            return out;
        }
    }

    record Sig(String name, ProtoId proto) {
    }

    private static Sig sig(LinkedMethod m) {
        return new Sig(m.name(), m.proto());
    }

    // =====================================================================
    // Linking
    // =====================================================================

    /**
     * Returns the LinkedClass for a type (cached). An iterative DFS over
     * dependencies (superclass + direct interfaces); a dependency in the
     * VISITING state means a cycle — it is not pushed, and the class closing
     * the cycle gets a link error. link() calls from inside computations are
     * allowed only for types outside the current stack (in the model —
     * Object and array components during post-link checks).
     */
    LinkedClass link(TypeId type) {
        // Classes being computed (computeLink): re-entrancy guard for the
        // on-demand linking of dependencies missing from the cache — re-entering
        // a class being computed means a real hierarchy cycle.
        var computing = new LinkedHashSet<TypeId>();
        return link(type, computing);
    }

    private LinkedClass link(TypeId type, Set<TypeId> computing) {
        var out = cache.get(type);
        if (out != null) {
            return out;
        }
        var stack = new ArrayDeque<TypeId>();
        var visiting = new LinkedHashSet<TypeId>();
        var expanded = new HashSet<TypeId>();
        stack.push(type);
        visiting.add(type);
        while (!stack.isEmpty()) {
            var t = stack.peek();
            if (cache.containsKey(t)) {
                stack.pop();
                visiting.remove(t);
                continue;
            }
            if (!expanded.add(t)) {
                // all dependencies processed — compute (t is still in
                // visiting: a dependency == t is recognized as a cycle)
                var lc = computeLink(t, computing);
                cache.put(t, lc);
                visiting.remove(t);
                stack.pop();
                continue;
            }
            var def = resolveClass(t);
            if (def == null) {
                continue; // terminal UNKNOWN — no dependencies
            }
            var deps = new ArrayList<TypeId>();
            if (!def.isInterface()) {
                var s = def.getSuperclass();
                if (s != null) {
                    deps.add(s);
                }
            }
            deps.addAll(def.getInterfaces());
            for (var dep : deps) {
                // computing-skip: a dependency being computed (nested
                // on-demand linking) — re-entry would mean a cycle
                if (cache.containsKey(dep) || visiting.contains(dep) || computing.contains(dep)) {
                    continue;
                }
                visiting.add(dep);
                stack.push(dep);
            }
        }
        return cache.get(type);
    }

    private LinkedClass computeLink(TypeId t, Set<TypeId> computing) {
        computing.add(t);
        try (var ignored = new AutoCloseable() {
            @Override
            public void close() {
                computing.remove(t);
            }
        }) {
            var def = resolveClass(t);
            var lc = new LinkedClass(t, def);
            if (def == null) {
                lc.exact = false;
                return lc;
            }

            // declared methods (dex order: direct, then virtual)
            var direct = new ArrayList<LinkedMethod>();
            var virtual = new ArrayList<LinkedMethod>();
            for (var m : def.getMethods()) {
                var lm = new LinkedMethod(lc, m, MethodKind.DECLARED);
                if (m.isDirect()) {
                    direct.add(lm);
                } else {
                    virtual.add(lm);
                }
            }
            lc.declaredDirect = direct;
            lc.declaredVirtual = virtual;

            // direct interfaces (already computed)
            var directIfaces = new ArrayList<LinkedClass>();
            for (var it : def.getInterfaces()) {
                var iLC = cache.get(it);
                if (iLC == null) {
                    if (computing.contains(it)) {
                        // a real cycle: the interface is being computed
                        lc.linkError = "ClassCircularityError: cyclic interface hierarchy at " + it;
                        return lc;
                    }
                    // not yet computed (deeper in the stack, owned by another
                    // class) — linked on demand
                    iLC = link(it, computing);
                }
                if (iLC.linkError != null) {
                    lc.linkError = "interface " + it + " failed to link: " + iLC.linkError;
                    return lc;
                }
                if (!iLC.isUnknown() && !iLC.isInterface()) {
                    // ART SetupInterfaceLookupTable: ICCE
                    lc.linkError = "IncompatibleClassChangeError: class " + t
                            + " implements non-interface class " + it;
                    return lc;
                }
                if (!iLC.exact) {
                    lc.exact = false;
                }
                directIfaces.add(iLC);
            }

            if (def.isInterface()) {
                computeInterfaceLink(lc, directIfaces);
                return lc;
            }

            var superType = def.getSuperclass();
            if (superType == null) {
                // the library guarantees: superclass == null only for j.l.Object
                if (!def.isJavaLangObject()) {
                    lc.linkError = "ClassFormatError: missing superclass";
                    return lc;
                }
                computeObjectLink(lc);
                return lc;
            }
            var superLC = cache.get(superType);
            if (superLC == null) {
                if (computing.contains(superType)) {
                    // a real superclass cycle closed by this class
                    lc.linkError = "ClassCircularityError: cyclic superclass hierarchy at " + superType;
                    return lc;
                }
                // not yet computed — linked on demand (a class in another
                // class's iface position is computed before its own superclass)
                superLC = link(superType, computing);
            }
            if (superLC.linkError != null) {
                lc.linkError = "superclass " + superType + " failed to link: " + superLC.linkError;
                return lc;
            }
            if (superLC.isInterface()) {
                lc.linkError = "IncompatibleClassChangeError: superclass " + superType + " is an interface";
                return lc;
            }
            if (!superLC.exact) {
                lc.exact = false;
            }
            lc.superLC = superLC;
            computeClassLink(lc, directIfaces);
            return lc;
        }
    }

    /**
     * j.l.Object: the vtable is the declared virtuals in order
     * (LinkJavaLangObjectMethods).
     */
    private void computeObjectLink(LinkedClass lc) {
        var vtable = new ArrayList<LinkedMethod>(lc.declaredVirtual.size());
        for (var m : lc.declaredVirtual) {
            m.methodIndex = vtable.size();
            vtable.add(m);
        }
        lc.vtable = vtable;
        lc.iftable = List.of();
    }

    /**
     * The interface branch of LinkMethods: virtual indexes, kAccDefault,
     * iftable, conflict copies.
     */
    private void computeInterfaceLink(LinkedClass lc, List<LinkedClass> directIfaces) {
        // an interface's superclass is j.l.Object: FindClassMethod on an
        // interface walks the superclass chain and reaches Object.clone (t56)
        lc.superLC = OBJ;

        boolean hasDefaults = false;
        int index = 0;
        for (var m : lc.declaredVirtual) {
            m.methodIndex = index++;
            if (m.isDefault()) {
                hasDefaults = true;
            }
        }
        lc.hasDefaultMethods = hasDefaults;
        if (lc.declaredVirtual.size() > 0xFFFF) {
            lc.linkError = "ClassFormatError: too many methods on interface";
            return;
        }
        lc.iftable = setupInterfaceLookupTable(lc, List.of(), directIfaces);
        boolean haveSuperWithDefaults = false;
        for (var e : lc.iftable) {
            if (e.iface.hasDefaultMethods) {
                haveSuperWithDefaults = true;
                break;
            }
        }
        if (haveSuperWithDefaults) {
            findCopiedMethodsForInterface(lc);
        }
    }

    /**
     * A port of SetupInterfaceLookupTable + FindNewIfTableInterfaces: the
     * super prefix, then the new interfaces (a direct one's superinterfaces
     * before itself). Order invariant: I &lt;: J ⇒ J before I. If nothing is
     * new, the same list is returned (the linking fast-path condition).
     */
    private List<IfTableEntry> setupInterfaceLookupTable(LinkedClass lc,
                                                         List<IfTableEntry> superIftable,
                                                         List<LinkedClass> directIfaces) {
        if (directIfaces.isEmpty()) {
            return superIftable;
        }
        var seen = new LinkedHashSet<TypeId>();
        for (var e : superIftable) {
            seen.add(e.iface.type);
        }
        var newEntries = new ArrayList<IfTableEntry>();
        for (var direct : directIfaces) {
            if (direct.isUnknown()) {
                // an unknown interface: its entry cannot be added
                lc.exact = false;
                continue;
            }
            if (seen.contains(direct.type)) {
                continue;
            }
            for (var e : direct.iftable) {
                if (seen.add(e.iface.type)) {
                    newEntries.add(new IfTableEntry(e.iface, null));
                }
            }
            seen.add(direct.type);
            newEntries.add(new IfTableEntry(direct, null));
        }
        if (newEntries.isEmpty()) {
            return superIftable;
        }
        var out = new ArrayList<IfTableEntry>(superIftable.size() + newEntries.size());
        out.addAll(superIftable);
        out.addAll(newEntries);
        return out;
    }

    /**
     * A port of FindCopiedMethodsForInterface: only conflict methods
     * (kDefaultConflict) are copied into a sub-interface; other records are
     * pruned (a default is looked up at the call via
     * FindVirtualMethodForInterfaceSuper).
     */
    private void findCopiedMethodsForInterface(LinkedClass lc) {
        var declaredBySig = new LinkedHashMap<Sig, LinkedMethod>();
        for (var m : lc.declaredVirtual) {
            declaredBySig.putIfAbsent(sig(m), m);
        }
        var records = new LinkedHashMap<Sig, CopiedMethodRecord>();
        int newMethodIndex = lc.declaredVirtual.size();
        int ifcount = lc.iftable.size();
        for (int i = ifcount - 1; i >= 0; i--) {
            var iface = lc.iftable.get(i).iface;
            if (!iface.hasDefaultMethods) {
                continue;
            }
            for (var im : iface.declaredVirtual) {
                if (!im.isDefault()) {
                    continue;
                }
                if (declaredBySig.containsKey(sig(im))) {
                    continue; // masked by an own declaration
                }
                var s = sig(im);
                var rec = records.get(s);
                if (rec == null) {
                    var nr = new CopiedMethodRecord(im, newMethodIndex);
                    nr.state = CopiedMethodRecord.State.DEFAULT;
                    // "pretend" a second default: a conflict unless masked
                    nr.updateStateForInterface(iface, im, lc, ifcount, i);
                    if (nr.state == CopiedMethodRecord.State.DEFAULT_CONFLICT) {
                        nr.state = CopiedMethodRecord.State.DEFAULT;
                        records.put(s, nr);
                        newMethodIndex++;
                    }
                } else {
                    rec.updateStateForInterface(iface, im, lc, ifcount, i);
                }
            }
        }
        var copies = new ArrayList<LinkedMethod>();
        for (var rec : records.values()) {
            if (rec.state != CopiedMethodRecord.State.DEFAULT_CONFLICT) {
                continue; // pruning
            }
            var copy = new LinkedMethod(rec.main.declaring, rec.main.def, MethodKind.DEFAULT_CONFLICT);
            copy.methodIndex = rec.index;
            copy.conflictCandidates = List.copyOf(rec.candidates);
            copies.add(copy);
        }
        lc.copied = copies;
        if (lc.declaredVirtual.size() + copies.size() > 0xFFFF) {
            lc.linkError = "ClassFormatError: too many methods on interface";
        }
    }

    /**
     * The class branch of LinkMethods: the fast path, or AssignVTableIndexes
     * + copies + FinalizeIfTable.
     */
    private void computeClassLink(LinkedClass lc, List<LinkedClass> directIfaces) {
        var superLC = lc.superLC;
        var superIftable = superLC.iftable;
        int superIfcount = superIftable.size();
        lc.iftable = setupInterfaceLookupTable(lc, superIftable, directIfaces);
        boolean sameIftable = lc.iftable == superIftable;
        int ifcount = lc.iftable.size();
        var superVtable = superLC.vtable;
        int superVtableLen = superVtable.size();
        int numVirtual = lc.declaredVirtual.size();

        // Fast path: no new virtuals and no new interfaces — the superclass
        // vtable and method arrays are reused as is
        if (numVirtual == 0 && sameIftable) {
            lc.vtable = superVtable;
            return;
        }
        if (sameIftable) {
            lc.iftable = new ArrayList<>(lc.iftable);
        }

        int vtableLen = superVtableLen;

        // declared virtuals by signature (first on duplicates)
        var declaredBySig = new LinkedHashMap<Sig, LinkedMethod>();
        for (var m : lc.declaredVirtual) {
            declaredBySig.putIfAbsent(sig(m), m);
        }

        // super vtable overrides (AssignVTableIndexes, first loop)
        var initialized = new HashSet<LinkedMethod>();
        int[] sameSigLists = new int[superVtableLen];
        Arrays.fill(sameSigLists, NO_INDEX);
        for (int j = 0; j < superVtableLen; j++) {
            var superMethod = superVtable.get(j);
            if (!canAccessMember(lc, superMethod, true)) {
                // package-private from another package is not overridden
                // (b/211854716); protected with an incomplete hierarchy —
                // fail-open (historical behavior)
                continue;
            }
            var match = declaredBySig.get(sig(superMethod));
            if (match == null) {
                continue;
            }
            if (superMethod.isFinal()) {
                lc.linkError = "LinkageError: Method " + match
                        + " overrides final method in class " + superMethod.declaring.type;
                return;
            }
            if (!initialized.add(match)) {
                // the method overrides several slots — a chain of former indexes
                sameSigLists[j] = match.methodIndex;
            }
            match.methodIndex = j; // the final index is the greatest j
        }
        for (var m : lc.declaredVirtual) {
            if (!initialized.contains(m)) {
                m.methodIndex = vtableLen++;
            }
        }

        // interface methods of new interfaces, reverse order (most specific first)
        var records = new LinkedHashMap<Sig, CopiedMethodRecord>();
        var rawArrays = new IntMap<int[]>();
        Map<Sig, LinkedMethod> publicSuperBySig = null;
        for (int i = ifcount - 1; i >= superIfcount; i--) {
            var entry = lc.iftable.get(i);
            var iface = entry.iface;
            int n = iface.declaredVirtual.size();
            if (n == 0) {
                continue;
            }
            var raw = new int[n];
            rawArrays.put(i, raw);
            for (var im : iface.declaredVirtual) {
                LinkedMethod vm = null;
                var dm = declaredBySig.get(sig(im));
                if (dm != null) {
                    if (dm.isPublic()) {
                        vm = dm; // interface overriding requires public
                    }
                } else {
                    if (publicSuperBySig == null) {
                        publicSuperBySig = new LinkedHashMap<>();
                        for (int k = 0; k < superVtableLen; k++) {
                            var sm = superVtable.get(k);
                            if (sm.isPublic()) {
                                publicSuperBySig.putIfAbsent(sig(sm), sm);
                            }
                        }
                    }
                    vm = publicSuperBySig.get(sig(im));
                }
                if (vm != null && !vm.isOverridableByDefaultMethod()) {
                    // a plain class method in the super vtable — a simple mapping, no copy
                    raw[im.methodIndex] = vm.methodIndex;
                    continue;
                }
                var s = sig(im);
                var rec = records.get(s);
                if (rec == null) {
                    rec = new CopiedMethodRecord(im, vm != null ? vm.methodIndex : vtableLen);
                    records.put(s, rec);
                    if (vm == null) {
                        vtableLen++;
                    }
                    rec.state = im.isAbstract()
                            ? CopiedMethodRecord.State.ABSTRACT_SINGLE
                            : CopiedMethodRecord.State.DEFAULT_SINGLE;
                    if (!im.isAbstract()) {
                        rec.candidates.add(im);
                    }
                } else {
                    rec.updateState(iface, im, lc, ifcount, i, rawArrays);
                }
                raw[im.methodIndex] = rec.index;
            }
        }

        // FinalizeState: records whose slot is already taken by a copy from the super vtable
        for (var rec : records.values()) {
            if (rec.index < superVtableLen) {
                var superMethod = superVtable.get(rec.index);
                rec.finalizeState(superMethod, lc, ifcount, superIftable, superIfcount, rawArrays);
            }
        }

        if (vtableLen > 0xFFFF) {
            lc.linkError = "ClassFormatError: too many methods defined on class";
            return;
        }

        // ReallocMethods: materializing the copies (sorted by vtable index)
        var copies = new ArrayList<LinkedMethod>();
        var sorted = new ArrayList<CopiedMethodRecord>();
        for (var rec : records.values()) {
            if (rec.state != CopiedMethodRecord.State.USE_SUPER) {
                sorted.add(rec);
            }
        }
        sorted.sort(Comparator.comparingInt(a -> a.index));
        for (var rec : sorted) {
            MethodKind kind = switch (rec.state) {
                case ABSTRACT_SINGLE, ABSTRACT -> MethodKind.MIRANDA;
                case DEFAULT_SINGLE, DEFAULT -> MethodKind.DEFAULT;
                case DEFAULT_CONFLICT -> MethodKind.DEFAULT_CONFLICT;
                default -> throw new IllegalStateException(rec.state.toString());
            };
            var copy = new LinkedMethod(rec.main.declaring, rec.main.def, kind);
            copy.methodIndex = rec.index;
            if (kind == MethodKind.DEFAULT_CONFLICT) {
                copy.conflictCandidates = List.copyOf(rec.candidates);
            }
            copies.add(copy);
        }
        // USE_SUPER — aliases (see LinkedClass.aliases); for conflicts,
        // the candidate lists are merged
        var useSuperAliases = new ArrayList<LinkedMethod>();
        for (var rec : records.values()) {
            if (rec.state != CopiedMethodRecord.State.USE_SUPER) {
                continue;
            }
            var superMethod = superVtable.get(rec.index);
            var alias = new LinkedMethod(superMethod.declaring, superMethod.def, superMethod.kind);
            alias.methodIndex = rec.index;
            if (superMethod.kind == MethodKind.DEFAULT_CONFLICT) {
                var merged = new ArrayList<>(superMethod.conflictCandidates);
                for (var c : rec.candidates) {
                    if (!merged.contains(c)) {
                        merged.add(c);
                    }
                }
                alias.conflictCandidates = List.copyOf(merged);
            }
            useSuperAliases.add(alias);
        }
        lc.copied = copies;
        lc.aliases = useSuperAliases;

        // filling the vtable (overrides + same-signature chains + superclass leftovers)
        var vtableArr = new LinkedMethod[vtableLen];
        for (var m : lc.allVirtuals()) {
            int vi = m.methodIndex;
            if (vi < 0 || vi >= vtableLen) {
                continue; // must not happen
            }
            vtableArr[vi] = m;
            if (vi < sameSigLists.length) {
                while (sameSigLists[vi] != NO_INDEX) {
                    vi = sameSigLists[vi];
                    vtableArr[vi] = m;
                }
            }
        }
        for (int j = 0; j < superVtableLen; j++) {
            if (vtableArr[j] == null) {
                vtableArr[j] = superVtable.get(j);
            }
        }
        var vtable = new ArrayList<>(Arrays.asList(vtableArr));
        lc.vtable = vtable;

        // FinalizeIfTable: the method arrays (the super range — re-dispatch
        // through the new vtable, the new range — stored vtable indexes
        // converted to methods)
        for (int i = 0; i < superIfcount; i++) {
            var superEntry = superIftable.get(i);
            var superArray = superEntry.methodArray;
            if (superArray == null) {
                continue;
            }
            var newArr = new ArrayList<LinkedMethod>(superArray.size());
            for (var impl : superArray) {
                newArr.add(impl.methodIndex >= 0 && impl.methodIndex < vtable.size()
                        ? vtable.get(impl.methodIndex) : impl);
            }
            lc.iftable.set(i, new IfTableEntry(superEntry.iface, newArr));
        }
        for (int i = superIfcount; i < ifcount; i++) {
            var raw = rawArrays.get(i);
            if (raw == null) {
                continue;
            }
            var newArr = new ArrayList<LinkedMethod>(raw.length);
            for (int idx : raw) {
                newArr.add(idx >= 0 && idx < vtable.size() ? vtable.get(idx) : null);
            }
            lc.iftable.set(i, new IfTableEntry(lc.iftable.get(i).iface, newArr));
        }
    }

    // =====================================================================
    // CopiedMethodRecord (a port of the state machine + candidate-completeness extension)
    // =====================================================================

    /**
     * A port of CopiedMethodRecord (runtime/class_linker.cc). Extension over
     * ART: in the DEFAULT_CONFLICT state, ART's UpdateStateImpl exits early;
     * here unmasked defaults keep being collected into candidates — a
     * DefaultConflict must carry all unmasked default candidates (masked
     * ones — no).
     */
    private final class CopiedMethodRecord {
        enum State {ABSTRACT_SINGLE, DEFAULT_SINGLE, ABSTRACT, DEFAULT, DEFAULT_CONFLICT, USE_SUPER}

        LinkedMethod main; // ART main_method_: the first default, else the first abstract
        int index;         // the record's vtable index
        State state;
        final List<LinkedMethod> candidates = new ArrayList<>();

        CopiedMethodRecord(LinkedMethod main, int index) {
            this.main = main;
            this.index = index;
            this.state = State.ABSTRACT_SINGLE;
        }

        /**
         * ART UpdateStateForInterface (interface conflict copies).
         */
        void updateStateForInterface(LinkedClass iface, LinkedMethod im,
                                     LinkedClass owner, int ifcount, int index) {
            if (containsOverridingMethodOf(owner, index + 1, ifcount, iface, im)) {
                return; // masked by a more specific interface
            }
            state = State.DEFAULT_CONFLICT;
            addCandidate(im);
        }

        /**
         * ART UpdateState (the class's new interfaces; target — the record's vtable index).
         */
        void updateState(LinkedClass iface, LinkedMethod im,
                         LinkedClass owner, int ifcount, int i, IntMap<int[]> rawArrays) {
            int target = index;
            updateStateImpl(iface, im, () -> containsImplementingMethod(
                    owner, i + 1, ifcount, iface, target, rawArrays));
        }

        /**
         * ART FinalizeState (record + super-vtable entry — a copy from the superclass).
         */
        void finalizeState(LinkedMethod superMethod, LinkedClass owner, int ifcount,
                           List<IfTableEntry> superIftable, int superIfcount, IntMap<int[]> rawArrays) {
            if (superMethod.isDefault()) { // kAccDefault: DEFAULT or DEFAULT_CONFLICT
                if (superMethod.isDefaultConflicting()) {
                    // some superclass conflict participants may be masked by
                    // the new interfaces — walk the super iftable participants
                    int i = superIfcount;
                    while (state != State.DEFAULT_CONFLICT && i != 0) {
                        --i;
                        var entry = owner.iftable.get(i);
                        var superArray = superIftable.get(i).methodArray;
                        if (superArray == null) {
                            continue;
                        }
                        var iface = entry.iface;
                        for (var m : iface.declaredVirtual) {
                            if (m.methodIndex >= 0 && m.methodIndex < superArray.size()
                                    && superArray.get(m.methodIndex) == superMethod) {
                                final int ii = i;
                                updateStateImpl(iface, m, () ->
                                        containsImplementingMethod(superIftable, ii + 1, superIfcount, iface, superMethod)
                                                || containsImplementingMethod(owner, superIfcount, ifcount, iface, index, rawArrays));
                                break;
                            }
                        }
                    }
                    if (state == State.DEFAULT_CONFLICT) {
                        state = State.USE_SUPER; // the conflict is reproduced — the superclass copy is reused
                    }
                } else {
                    // a single default of the superinterfaces, not masked by sub-interfaces
                    updateState(superMethod.declaring, superMethod, owner, ifcount, superIfcount - 1, rawArrays);
                    if (main == superMethod) {
                        state = State.USE_SUPER;
                    }
                }
            } else {
                // the superclass miranda is reused
                if (state == State.ABSTRACT_SINGLE || state == State.ABSTRACT) {
                    state = State.USE_SUPER;
                }
            }
        }

        /**
         * A port of UpdateStateImpl (runtime/class_linker.cc) — the state
         * transitions verbatim; candidate collection — see the extension in
         * the class doc.
         */
        private void updateStateImpl(LinkedClass iface, LinkedMethod im, BooleanSupplier slowIsMasked) {
            boolean haveDefault = state == State.DEFAULT_SINGLE || state == State.DEFAULT;
            switch (state) {
                case DEFAULT_SINGLE, ABSTRACT_SINGLE -> {
                    if (implementsIface(main.declaring, iface, true)) {
                        return; // main masks im
                    }
                    if (!im.isDefault()) {
                        state = haveDefault ? State.DEFAULT : State.ABSTRACT;
                        return;
                    }
                    // default — falls through without the slow check (the *Single fast path)
                }
                case DEFAULT, ABSTRACT -> {
                    if (!im.isDefault()) {
                        return;
                    }
                    if (slowIsMasked.getAsBoolean()) {
                        return; // masked
                    }
                }
                case DEFAULT_CONFLICT -> {
                    // ART: the state does not change. Extension: an unmasked
                    // default joins the conflict candidates
                    if (!im.isDefault()) {
                        return;
                    }
                    if (slowIsMasked.getAsBoolean()) {
                        return;
                    }
                    addCandidate(im);
                    return;
                }
                default -> {
                    return;
                }
            }
            // a "new unmasked default"
            if (haveDefault) {
                state = State.DEFAULT_CONFLICT;
                addCandidate(im);
            } else {
                main = im;
                state = State.DEFAULT;
                candidates.clear();
                addCandidate(im);
            }
        }

        private void addCandidate(LinkedMethod m) {
            if (!candidates.contains(m)) {
                candidates.add(m);
            }
        }
    }

    // =====================================================================
    // Masking/access/assignability
    // =====================================================================

    /**
     * ART ContainsImplementingMethod, the iftable new range: the new
     * interfaces' method arrays still hold vtable indexes (rawArrays).
     */
    private boolean containsImplementingMethod(LinkedClass owner, int begin, int end,
                                               LinkedClass iface, int target, IntMap<int[]> rawArrays) {
        for (int i = begin; i < end; i++) {
            var raw = rawArrays.get(i);
            if (raw == null) {
                continue;
            }
            boolean contains = false;
            for (int k : raw) {
                if (k == target) {
                    contains = true;
                    break;
                }
            }
            if (contains && implementsIface(owner.iftable.get(i).iface, iface, true)) {
                return true;
            }
        }
        return false;
    }

    /**
     * ART ContainsImplementingMethod, the super range (the target is the
     * method itself).
     */
    private boolean containsImplementingMethod(List<IfTableEntry> iftable, int begin, int end,
                                               LinkedClass iface, LinkedMethod target) {
        for (int i = begin; i < end; i++) {
            var arr = iftable.get(i).methodArray;
            if (arr == null) {
                continue;
            }
            if (arr.contains(target) && implementsIface(iftable.get(i).iface, iface, true)) {
                return true;
            }
        }
        return false;
    }

    /**
     * ART ContainsOverridingMethodOf: declared methods of more specific
     * interfaces.
     */
    private boolean containsOverridingMethodOf(LinkedClass owner, int begin, int end,
                                               LinkedClass iface, LinkedMethod im) {
        for (int i = begin; i < end; i++) {
            var cur = owner.iftable.get(i).iface;
            for (var m : cur.declaredVirtual) {
                if (m.matches(im.name(), im.proto())) {
                    if (implementsIface(cur, iface, true)) {
                        return true;
                    }
                    break;
                }
            }
        }
        return false;
    }

    /**
     * A port of Class::CanAccessMember (runtime/mirror/class-inl.h) by member
     * flags (methods and fields). Used for the vtable-entry override gate and
     * for the referrer's access to a resolved member in
     * ResolveMethodWithChecks / ResolvedFieldAccessTest.
     *
     * <p>Returns Boolean: null — the answer is non-determinable. The
     * public/private and package-private branches are determined by type
     * descriptors and flags (the package — TypeId.isInSamePackage, the model
     * has a single class loader); only the protected branch is
     * non-determinable: it needs IsAssignableFrom(declarer, referrer), and
     * the referrer hierarchy may be unknown (a class outside the classpath)
     * or incomplete (exact == false).
     */
    Boolean canAccessMember(LinkedClass accessFrom, LinkedClass accessTo, int flags) {
        if (accessFrom.type.equals(accessTo.type)) {
            return true;
        }
        if ((flags & ACC_PUBLIC) != 0) {
            return true;
        }
        if ((flags & ACC_PRIVATE) != 0) {
            return false;
        }
        if ((flags & ACC_PROTECTED) != 0) {
            // a single package settles protected regardless of assignability
            if (TypeId.isInSamePackage(accessFrom.type, accessTo.type)) {
                return true;
            }
            // documented ART leniency: the declarer instead of the receiver type (JLS 6.6.2)
            return isAssignableFrom(accessTo, accessFrom);
        }
        return TypeId.isInSamePackage(accessFrom.type, accessTo.type);
    }

    /**
     * The defaultValue variant: a non-determinable answer (null) is shifted
     * to the given side. The linking checks (the vtable override gate) are
     * historically fail-open — defaultValue = true.
     */
    @SuppressWarnings("SameParameterValue")
    boolean canAccessMember(LinkedClass accessFrom, LinkedClass accessTo, int flags,
                            boolean defaultValue) {
        var access = canAccessMember(accessFrom, accessTo, flags);
        return access != null ? access : defaultValue;
    }

    /**
     * Method access — the flags from def (see the main overload).
     */
    @SuppressWarnings("SameParameterValue")
    boolean canAccessMember(LinkedClass accessFrom, LinkedMethod member, boolean defaultValue) {
        return canAccessMember(accessFrom, member.declaring,
                member.def.getAccessFlags(), defaultValue);
    }

    /**
     * A port of Class::CanAccess (runtime/mirror/class-inl.h):
     * this == that || that.IsPublic() || same package. Used from
     * ResolveMethodWithChecks (referrer → the resolved method's declarer
     * class, with the MethodId class escape hatch). Returns Boolean: null —
     * the answer is non-determinable (the target is an unknown class: its
     * publicity is invisible; the package is known from the descriptor and
     * checked first). The check does not depend on the referrer hierarchy
     * (only the target's descriptors and flags) — deterministic for an
     * unknown referrer.
     */
    Boolean canAccess(TypeId from, LinkedClass to) {
        if (from.equals(to.type)) {
            return true;
        }
        if (TypeId.isInSamePackage(from, to.type)) {
            return true;
        }
        if (to.def == null) {
            return null; // an unknown class's publicity is invisible — non-determinable
        }
        return (to.def.getAccessFlags() & ACC_PUBLIC) != 0;
    }

    /**
     * The defaultValue variant: a non-determinable answer (null) is shifted
     * to the given side.
     */
    @SuppressWarnings("unused")
    boolean canAccess(TypeId from, LinkedClass to, boolean defaultValue) {
        var access = canAccess(from, to);
        return access != null ? access : defaultValue;
    }

    /**
     * A port of ArtMethod::ThrowInvocationTimeError (runtime/art_method.cc) —
     * picking the execution error for a non-invokable target (the target is
     * found; the error is not a resolution one and is returned with the
     * method). receiver == null (a static call) — no miranda walk, as in ART.
     */
    InvocationError throwInvocationTimeError(LinkedMethod called, LinkedClass receiver) {
        if (called.isDefaultConflicting()) {
            return InvocationError.INCOMPATIBLE_CLASS_CHANGE;
        }
        if (called.declaring.isInterface() && receiver != null) {
            // miranda walk: the receiver chain's declared methods
            // (direct+virtual, no copied), non-static, same signature; the
            // first found decides: non-public → IAE, public abstract → AME;
            // public concrete — looked past (no separate branch, as in ART)
            for (var current = receiver; current != null; current = current.superLC) {
                for (var m : current.allDeclared()) {
                    if (!m.isStatic() && m.matches(called.name(), called.proto())) {
                        if (!m.isPublic()) {
                            return InvocationError.ILLEGAL_ACCESS;
                        }
                        if (m.isAbstract()) {
                            return InvocationError.ABSTRACT_METHOD;
                        }
                    }
                }
            }
        }
        return InvocationError.ABSTRACT_METHOD;
    }

    /**
     * A port of Class::IsAssignableFrom (runtime/mirror/class-inl.h).
     * Returns Boolean: null — the answer is non-determinable
     * (an unknown src superclass-chain node or its incomplete
     * iftable can neither confirm nor exclude assignability).
     * Arrays are not the linker's domain.
     */
    Boolean isAssignableFrom(LinkedClass target, LinkedClass src) {
        var t = target;
        var s = src;

        assert !t.type.isArray();
        assert !t.type.isPrimitive();
        assert !s.type.isArray();
        assert !s.type.isPrimitive();

        if (t == s || t.type.equals(s.type)) {
            return true;
        }
        if (t.type.equals(TypeId.OBJECT)) {
            return !s.type.isPrimitive();
        }
        if (t.isInterface()) {
            return implementsIface(s, t);
        }
        if (s.isInterface()) {
            return false;
        }
        return isSubClass(s, t);
    }

    /**
     * The defaultValue variant: a non-determinable answer (null) is shifted
     * to the given side (the linking checks and JLS domination are
     * historically fail-open, true).
     */
    @SuppressWarnings("SameParameterValue")
    boolean isAssignableFrom(LinkedClass target, LinkedClass src, boolean defaultValue) {
        var assignable = isAssignableFrom(target, src);
        return assignable != null ? assignable : defaultValue;
    }

    /**
     * A port of Class::Implements (runtime/mirror/class-inl.h): a linear
     * iftable scan. The iftable covers only the known part of the hierarchy:
     * any unknown (an unknown ancestor — its iftable is empty, an unknown
     * direct interface — the entry is skipped, an unknown deeper in the
     * interface tree) transitively marks the class exact=false. Invariant:
     * exact == false <=> the iftable is incomplete — hence on a miss with an
     * incomplete iftable the answer is non-determinable (null), with a
     * complete one — false.
     */
    private Boolean implementsIface(LinkedClass src, LinkedClass iface) {
        for (var e : src.iftable) {
            if (e.iface.type.equals(iface.type)) {
                return true;
            }
        }
        return src.exact ? false : null; // an incomplete iftable — cannot exclude
    }

    /**
     * The defaultValue variant: a non-determinable answer (null) is shifted
     * to the given side (masking during linking is historically fail-open,
     * true).
     */
    @SuppressWarnings("SameParameterValue")
    private boolean implementsIface(LinkedClass src, LinkedClass iface, boolean defaultValue) {
        var impl = implementsIface(src, iface);
        return impl != null ? impl : defaultValue;
    }

    /**
     * A port of Class::IsSubClass (runtime/mirror/class-inl.h): a walk over
     * the superclass chain. An unknown class in the chain (def == null)
     * makes the answer non-determinable (null): the real hierarchy could
     * continue down to target (an unknown ancestor marks the whole known
     * prefix exact=false). null is always translated by the
     * isAssignableFrom callers.
     */
    private Boolean isSubClass(LinkedClass src, LinkedClass target) {
        for (var c = src; c != null; c = c.superLC) {
            if (c == target) {
                return true;
            }
            if (c.def == null) {
                return null; // an unknown hierarchy part — can neither confirm nor exclude
            }
        }
        return false;
    }

    // =====================================================================
    // Method lookup
    // =====================================================================

    /**
     * A port of Class::FindClassMethodWithSignature (runtime/mirror/class.cc):
     * declared → the superclass chain (the first candidate: inherited —
     * return, otherwise stop and end_klass) → copied methods below
     * end_klass → the uninherited candidate.
     *
     * <p>Answer exactness: a hit among start's declared methods or in the
     * known part of the chain is EXACT (the search consults them first, and
     * the unknown part of the hierarchy — superclasses above and interfaces
     * — cannot shadow such an answer). The copied layer, the uninherited
     * return and a miss depend on the completeness of start's hierarchy (an
     * unknown interface could add a copy) — for them exact degrades to
     * start.exact.
     */
    LinkedMethod findClassMethod(LinkedClass start, String name, ProtoId proto, boolean[] exact) {
        for (var m : start.allDeclared()) {
            if (m.matches(name, proto)) {
                return m; // own declaration — exact answer
            }
        }
        LinkedMethod uninherited = null;
        LinkedClass endKlass = null;
        for (var k = start.superLC; k != null; k = k.superLC) {
            if (k.isUnknown()) {
                exact[0] = false;
                break; // the chain beyond is invisible
            }
            LinkedMethod candidate = null;
            for (var m : k.declaredDirect) {
                if (m.matches(name, proto)) {
                    candidate = m;
                    break;
                }
            }
            if (candidate == null) {
                for (var m : k.declaredVirtual) {
                    if (m.matches(name, proto)) {
                        candidate = m;
                        break;
                    }
                }
            }
            if (candidate != null) {
                if (isInheritedMethod(start, k, candidate)) {
                    return candidate; // the chain up to the declarer is fully known — exact
                }
                uninherited = candidate;
                endKlass = k;
                break;
            }
        }
        // the answers below depend on the copied layer (unknown interfaces
        // could add/change copies) — conservative degradation
        exact[0] &= start.exact;
        for (var k = start; k != null && k != endKlass; k = k.superLC) {
            for (var m : k.copied) {
                if (m.matches(name, proto)) {
                    return m; // copied entries are inherited by construction
                }
            }
        }
        return uninherited;
    }

    /**
     * A port of Class::IsInheritedMethod (runtime/mirror/class.cc):
     * public/protected are inherited, private — no, package-private — with
     * the same package along the whole chain.
     */
    private static boolean isInheritedMethod(LinkedClass klass,
                                             LinkedClass declaring,
                                             LinkedMethod method) {
        var flags = method.def.getAccessFlags();
        if ((flags & (ACC_PUBLIC | ACC_PROTECTED)) != 0) {
            return true;
        }
        if ((flags & ACC_PRIVATE) != 0) {
            return false;
        }
        // package-private: one package along the whole chain from klass to declaring
        for (var k = klass; k != null && k != declaring; k = k.superLC) {
            if (!TypeId.isInSamePackage(k.type, declaring.type)) {
                return false;
            }
        }
        return true;
    }

    /**
     * A port of Class::FindInterfaceMethodWithSignature
     * (runtime/mirror/class.cc): the interface's declared methods without a
     * kind filter (direct — private/static — are visible too), then the
     * iftable (allMethods, the isVirtual filter), then j.l.Object's public
     * non-static methods.
     */
    LinkedMethod findInterfaceMethodWithSignature(LinkedClass klass, String name,
                                                  ProtoId proto, boolean[] exact) {
        if (klass.isInterface()) {
            for (var m : klass.allDeclared()) {
                if (m.matches(name, proto)) {
                    return m; // own declaration — exact answer
                }
            }
        }
        // the iftable may be incomplete (unknown branches skipped at build) —
        // hits in it and the later phases are conservatively inexact
        exact[0] &= klass.exact;
        for (var entry : klass.iftable) {
            var iface = entry.iface;
            if (iface.isUnknown()) {
                exact[0] = false;
                continue;
            }
            for (var m : iface.allMethods()) {
                if (m.isVirtual() && m.matches(name, proto)) {
                    return m;
                }
            }
        }
        if (klass.isInterface()) {
            var obj = OBJ;
            if (obj.isUnknown()) {
                exact[0] = false;
            } else {
                for (var m : obj.allDeclared()) {
                    if (m.isPublic() && !m.isStatic() && m.matches(name, proto)) {
                        return m;
                    }
                }
            }
        }
        return null;
    }

    /**
     * A port of Class::FindVirtualMethodForInterface
     * (runtime/mirror/class-inl.h): itable dispatch — the receiver's iftable
     * entry for the declaring interface, the method array at the index.
     */
    LinkedMethod findVirtualMethodForInterface(LinkedMethod method, LinkedClass receiver) {
        for (var entry : receiver.iftable) {
            if (entry.iface.type.equals(method.declaring.type)) {
                var arr = entry.methodArray;
                if (arr == null || method.methodIndex < 0 || method.methodIndex >= arr.size()) {
                    return null; // exotic: a copy with a foreign local index
                }
                return arr.get(method.methodIndex);
            }
        }
        return null;
    }

    /**
     * A port of Class::FindVirtualMethodForInterfaceSuper
     * (runtime/mirror/class.cc): the reference interface's own methods
     * (including conflict copies), then the reverse iftable walk with JLS
     * domination (a default not overridden by a more specific abstract
     * wins; otherwise an arbitrary abstract).
     */
    LinkedMethod findVirtualMethodForInterfaceSuper(LinkedClass ifaceLC,
                                                    LinkedMethod method,
                                                    boolean[] exact) {
        for (var m : ifaceLC.allDeclared()) {
            if (m.isVirtual() && m.matches(method.name(), method.proto())) {
                return m; // own declaration — exact answer
            }
        }
        // conflict copies and the iftable walk depend on hierarchy completeness
        exact[0] &= ifaceLC.exact;
        for (var m : ifaceLC.copied) {
            if (m.isVirtual() && m.matches(method.name(), method.proto())) {
                return m;
            }
        }
        var abstractMethods = new ArrayList<LinkedMethod>();
        int ifcount = ifaceLC.iftable.size();
        for (int k = ifcount; k != 0; ) {
            k--;
            var entry = ifaceLC.iftable.get(k);
            var iface = entry.iface;
            if (iface.isUnknown()) {
                exact[0] = false;
                continue;
            }
            for (var m : iface.declaredVirtual) {
                if (!m.matches(method.name(), method.proto())) {
                    continue;
                }
                if (m.isDefault()) {
                    boolean overridden = false;
                    for (var possible : abstractMethods) {
                        if (isAssignableFrom(iface, possible.declaring, true)) {
                            overridden = true;
                            break;
                        }
                    }
                    if (!overridden) {
                        return m;
                    }
                } else {
                    abstractMethods.add(m);
                }
            }
        }
        return abstractMethods.isEmpty() ? null : abstractMethods.get(0);
    }
}
