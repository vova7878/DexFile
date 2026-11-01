package com.v7878.dex.analysis;

import static com.v7878.dex.analysis.Register.ConstantKind.BOOL;
import static com.v7878.dex.analysis.Register.ConstantKind.BYTE;
import static com.v7878.dex.analysis.Register.ConstantKind.CHAR;
import static com.v7878.dex.analysis.Register.ConstantKind.INT;
import static com.v7878.dex.analysis.Register.ConstantKind.NULL;
import static com.v7878.dex.analysis.Register.ConstantKind.POSITIVE_BYTE;
import static com.v7878.dex.analysis.Register.ConstantKind.POSITIVE_INT;
import static com.v7878.dex.analysis.Register.ConstantKind.POSITIVE_SHORT;
import static com.v7878.dex.analysis.Register.ConstantKind.REFERENCE;
import static com.v7878.dex.analysis.Register.ConstantKind.REFERENCE_OR_NULL;
import static com.v7878.dex.analysis.Register.ConstantKind.SHORT;
import static com.v7878.dex.analysis.Register.ConstantKind.WIDE_HI;
import static com.v7878.dex.analysis.Register.ConstantKind.WIDE_LO;
import static com.v7878.dex.analysis.Register.ConstantKind.ZERO;
import static com.v7878.dex.immutable.TypeId.OBJECT;
import static com.v7878.dex.util.Checks.shouldNotReachHere;
import static com.v7878.dex.util.ShortyUtils.invalidShorty;

import com.v7878.dex.immutable.TypeId;
import com.v7878.dex.util.Ids;

import java.util.Objects;

/**
 * A value stored in a register at one position of the method analysis:
 * a constant, a primitive or reference value, or one of the special
 * states (undefined, conflict, a broken wide pair).
 */
public sealed abstract class Register {
    /**
     * Where the value came from: the address of the instruction and the
     * register slot, plus {@code produced}, which is true only when that
     * instruction produced the value. It is false for a merge point
     * (the value is joined from several predecessors) and for an
     * instruction that only refined the value flowing into it (a null
     * check, a constructor call); such a value belongs to the instruction
     * that produced it. Parameters use address -1.
     */
    public record Identifier(int address, int slot, boolean produced) {
        public Identifier {
            if (address != -1 && address < 0) {
                throw new IllegalArgumentException(
                        "Invalid address: " + address);
            }
            if (produced && address < 0) {
                throw new IllegalArgumentException(
                        "Invalid produced value address: " + address);
            }
        }

        public Identifier(int address, int slot) {
            // A parameter address identifies a value no instruction produced
            this(address, slot, address >= 0);
        }

        public boolean isParameter() {
            return address == -1;
        }

        @Override
        public String toString() {
            return (address < 0 ? "parameter" : Integer.toHexString(address)) + "[" + slot + "]"
                    + (produced ? "" : "<incoming>");
        }
    }

    public abstract Identifier getSource();

    /**
     * Local registers in which nothing was stored yet.
     */
    public static final class Undefined extends Register {
        public static final Undefined INSTANCE = new Undefined();

        private Undefined() {
        }

        @Override
        public Identifier getSource() {
            return null;
        }

        @Override
        public String toString() {
            return "undefined";
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            return obj instanceof Undefined;
        }

        @Override
        public int hashCode() {
            return Undefined.class.hashCode();
        }
    }

    public static sealed abstract class DynamicRegister extends Register {
        private final Identifier source;

        public DynamicRegister(Identifier source) {
            this.source = Objects.requireNonNull(source);
        }

        @Override
        public Identifier getSource() {
            return source;
        }

        protected final String toString(String base) {
            return base + ":" + source;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            return obj instanceof DynamicRegister other
                    && Objects.equals(source, other.source);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(source);
        }
    }

    /**
     * Incompatible values merged into the same register.
     */
    public static final class Conflict extends DynamicRegister {
        private Conflict(Identifier source) {
            super(source);
        }

        public static Conflict of(Identifier source) {
            return new Conflict(source);
        }

        @Override
        public String toString() {
            return toString("conflict");
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (!super.equals(obj)) return false;
            return obj instanceof Conflict;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), Conflict.class);
        }
    }

    /**
     * A piece of broken wide pair.
     */
    public static final class WidePiece extends DynamicRegister {
        private WidePiece(Identifier source) {
            super(source);
        }

        public static WidePiece of(Identifier source) {
            return new WidePiece(source);
        }

        @Override
        public String toString() {
            return toString("W");
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (!super.equals(obj)) return false;
            return obj instanceof WidePiece;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), WidePiece.class);
        }
    }

    /**
     * The kind of a constant: the value range it is known to fit in,
     * or a non-numeric constant (string, class, ...). The is*()
     * queries widen the ranges.
     */
    public enum ConstantKind {
        // Constant [0]
        // Merged [0]
        ZERO,
        // Constant [1]
        // Merged [0 - 1]
        BOOL,
        // Constant [2 - 0x7f]
        // Merged [0 - 0x7f]
        POSITIVE_BYTE,
        // Constant [0x80 - 0x7fff]
        // Merged [0 - 0x7fff]
        POSITIVE_SHORT,
        // Constant [0x8000 - 0xffff]
        // Merged [0 - 0xffff]
        CHAR,
        // Constant [0x10000 - 0x7fffffff]
        // Merged [0 - 0x7fffffff]
        POSITIVE_INT,
        // Constant [-0x80 - -1]
        // Merged [-0x80 - 0x7f]
        BYTE,
        // Constant [-0x8000 - -0x81]
        // Merged [-0x8000 - 0x7fff]
        SHORT,
        // Constant [-0x80000000 - -0x8001]
        // Merged [-0x80000000 - 0x7fffffff]
        INT,
        WIDE_LO,
        WIDE_HI,
        STRING,
        // A string or null
        STRING_OR_NULL,
        CLASS,
        // A class or null
        CLASS_OR_NULL,
        METHOD_TYPE,
        // A method type or null
        METHOD_TYPE_OR_NULL,
        METHOD_HANDLE,
        // A method handle or null
        METHOD_HANDLE_OR_NULL,
        REFERENCE,
        // A reference of any type or null
        REFERENCE_OR_NULL,
        // Result of dereferencing null
        NULL;

        public boolean isZero() {
            return this == ZERO;
        }

        public boolean isBool() {
            return isZero() || this == BOOL;
        }

        public boolean isPositiveByte() {
            return isBool()
                    || this == POSITIVE_BYTE;
        }

        public boolean isPositiveShort() {
            return isPositiveByte()
                    || this == POSITIVE_SHORT;
        }

        public boolean isChar() {
            return isPositiveShort()
                    || this == CHAR;
        }

        public boolean isPositiveInt() {
            return isChar()
                    || this == POSITIVE_INT;
        }

        public boolean isByte() {
            return isPositiveByte()
                    || this == BYTE;
        }

        public boolean isShort() {
            return isPositiveShort()
                    || this == BYTE
                    || this == SHORT;
        }

        public boolean isInt() {
            return isPositiveInt()
                    || this == BYTE
                    || this == SHORT
                    || this == INT;
        }

        public boolean isWideLo() {
            return this == WIDE_LO;
        }

        public boolean isWideHi() {
            return this == WIDE_HI;
        }

        public boolean isWide() {
            return isWideLo() || isWideHi();
        }

        public boolean isString() {
            return this == STRING || this == STRING_OR_NULL;
        }

        public boolean isClass() {
            return this == CLASS || this == CLASS_OR_NULL;
        }

        public boolean isMethodType() {
            return this == METHOD_TYPE || this == METHOD_TYPE_OR_NULL;
        }

        public boolean isMethodHandle() {
            return this == METHOD_HANDLE || this == METHOD_HANDLE_OR_NULL;
        }

        /**
         * Whether the kind is a non-null reference constant. Nullable
         * kinds answer false: their value may be the null reference.
         */
        public boolean isNonZeroOrNullRef() {
            return this == STRING
                    || this == CLASS
                    || this == METHOD_TYPE
                    || this == METHOD_HANDLE
                    || this == REFERENCE;
        }

        /**
         * Whether the kind is a reference constant that also covers the
         * null reference (a nullable kind).
         */
        public boolean isNullable() {
            return switch (this) {
                case STRING_OR_NULL, CLASS_OR_NULL, METHOD_TYPE_OR_NULL,
                     METHOD_HANDLE_OR_NULL, REFERENCE_OR_NULL -> true;
                default -> false;
            };
        }

        /**
         * Whether the kind is a reference constant that is not guaranteed
         * non-null: the null reference, zero, or a nullable kind.
         */
        public boolean isMaybeNullRef() {
            return isZeroOrNull() || isNullable();
        }

        /**
         * The same kind widened with the null reference; kinds that are not
         * non-null reference constants are returned unchanged.
         */
        public ConstantKind withNull() {
            return switch (this) {
                case STRING -> STRING_OR_NULL;
                case CLASS -> CLASS_OR_NULL;
                case METHOD_TYPE -> METHOD_TYPE_OR_NULL;
                case METHOD_HANDLE -> METHOD_HANDLE_OR_NULL;
                case REFERENCE -> REFERENCE_OR_NULL;
                default -> this;
            };
        }

        /**
         * The same kind without the null reference; kinds that are not
         * nullable are returned unchanged.
         */
        public ConstantKind withoutNull() {
            return switch (this) {
                case STRING_OR_NULL -> STRING;
                case CLASS_OR_NULL -> CLASS;
                case METHOD_TYPE_OR_NULL -> METHOD_TYPE;
                case METHOD_HANDLE_OR_NULL -> METHOD_HANDLE;
                case REFERENCE_OR_NULL -> REFERENCE;
                default -> this;
            };
        }

        /**
         * Whether the non-null values of the kind are exactly instances of
         * the class it denotes: the classes of a string, class and method
         * type constant are final, while a method handle is abstract and a
         * plain reference is Object.
         */
        public boolean isExactType() {
            return switch (this) {
                case STRING, STRING_OR_NULL, CLASS, CLASS_OR_NULL,
                     METHOD_TYPE, METHOD_TYPE_OR_NULL -> true;
                default -> false;
            };
        }

        public boolean isNull() {
            return this == NULL;
        }

        public boolean isZeroOrNull() {
            return isZero() || isNull();
        }

        public boolean isRef() {
            return isMaybeNullRef()
                    || isNonZeroOrNullRef();
        }

        @Override
        public String toString() {
            return switch (this) {
                case ZERO -> "zero";
                case BOOL -> "boolean";
                case POSITIVE_BYTE -> "+byte";
                case POSITIVE_SHORT -> "+short";
                case CHAR -> "char";
                case POSITIVE_INT -> "+int";
                case BYTE -> "byte";
                case SHORT -> "short";
                case INT -> "int";
                case WIDE_LO -> "<wide";
                case WIDE_HI -> ">wide";
                case STRING -> "string";
                case STRING_OR_NULL -> "string|null";
                case CLASS -> "class";
                case CLASS_OR_NULL -> "class|null";
                case METHOD_TYPE -> "method type";
                case METHOD_TYPE_OR_NULL -> "method type|null";
                case METHOD_HANDLE -> "method handle";
                case METHOD_HANDLE_OR_NULL -> "method handle|null";
                case REFERENCE -> "reference";
                case REFERENCE_OR_NULL -> "reference|null";
                case NULL -> "null";
            };
        }
    }

    /**
     * The narrowest constant kind that always holds the value.
     */
    public static ConstantKind intKind(int value) {
        if (value == 0) {
            return ZERO;
        } else if (value == 1) {
            return BOOL;
        } else if (value >= 2 && value <= 0x7f) {
            return POSITIVE_BYTE;
        } else if (value >= 0x80 && value <= 0x7fff) {
            return POSITIVE_SHORT;
        } else if (value >= 0x8000 && value <= 0xffff) {
            return CHAR;
        } else if (value >= 0x10000) {
            return POSITIVE_INT;
        } else if (value >= -0x80) {
            return BYTE;
        } else if (value >= -0x8000) {
            return SHORT;
        }
        return INT;
    }

    /**
     * A register with a known {@link TypeInfo}.
     */
    public static abstract sealed class TypedRegister extends DynamicRegister {
        private final TypeInfo type_info;

        private TypedRegister(Identifier source, TypeInfo type_info) {
            super(source);
            this.type_info = Objects.requireNonNull(type_info);
        }

        /* package */ TypeInfo typeInfo() {
            return type_info;
        }

        /* package */ char getShorty() {
            return type_info.getShorty();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (!super.equals(obj)) return false;
            return obj instanceof TypedRegister other
                    && Objects.equals(type_info, other.type_info);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), type_info);
        }
    }

    /**
     * A constant value, classified by {@link ConstantKind}.
     */
    public static final class Constant extends DynamicRegister {
        private final ConstantKind kind;

        private Constant(Identifier source, ConstantKind kind) {
            super(source);
            this.kind = Objects.requireNonNull(kind);
        }

        public static Constant of(Identifier source, ConstantKind kind) {
            return new Constant(source, kind);
        }

        public static Constant of(Identifier source, int value) {
            return of(source, intKind(value));
        }

        public static Constant of(Identifier source, boolean lo) {
            return of(source, lo ? WIDE_LO : WIDE_HI);
        }

        /* package */ ConstantKind classify() {
            return kind;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (!super.equals(obj)) return false;
            return obj instanceof Constant other
                    && Objects.equals(kind, other.kind);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), kind);
        }

        @Override
        public String toString() {
            return toString("~" + classify());
        }
    }

    /**
     * A non-wide primitive value: Z, B, S, C, I or F.
     */
    public static final class Primitive extends TypedRegister {
        private Primitive(Identifier source, TypeId type) {
            super(source, TypeInfo.of(type));
            Objects.requireNonNull(type);
            char shorty = type.getShorty();
            switch (shorty) {
                case 'Z', 'B', 'S', 'C', 'I', 'F' -> { /* nop */ }
                default -> throw invalidShorty(shorty);
            }
        }

        public static Primitive of(Identifier source, TypeId type) {
            return new Primitive(source, type);
        }

        @Override
        public String toString() {
            return toString(typeInfo().toString());
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (!super.equals(obj)) return false;
            return obj instanceof Primitive;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), Primitive.class);
        }
    }

    /**
     * A half of a wide (J or D) value.
     */
    public static final class WidePrimitive extends TypedRegister {
        private final boolean lo;

        private WidePrimitive(Identifier source, TypeId type, boolean lo) {
            super(source, TypeInfo.of(type));
            Objects.requireNonNull(type);
            char shorty = type.getShorty();
            switch (shorty) {
                case 'J', 'D' -> { /* nop */ }
                default -> throw invalidShorty(shorty);
            }
            this.lo = lo;
        }

        public static WidePrimitive of(Identifier source, TypeId type, boolean lo) {
            return new WidePrimitive(source, type, lo);
        }

        /* package */ boolean isLo() {
            return lo;
        }

        /* package */ boolean isHi() {
            return !isLo();
        }

        @Override
        public String toString() {
            return toString((lo ? "<" : ">") + typeInfo());
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (!super.equals(obj)) return false;
            return obj instanceof WidePrimitive other
                    && lo == other.lo;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), lo);
        }
    }

    /**
     * A reference value with a possibly unresolved type.
     */
    public static sealed class Reference extends TypedRegister {
        private final boolean non_null;
        private final boolean exact;

        private Reference(Identifier source, TypeInfo type, boolean non_null, boolean exact) {
            super(source, type);
            if (type.isPrimitive()) {
                throw new IllegalArgumentException(
                        type + " must not be primitive");
            }
            assert !exact || !type.isUnresolved();
            this.non_null = non_null;
            this.exact = exact;
        }

        /**
         * @param exact whether a non-null value is exactly of the type, not
         *              of a subtype of it
         */
        public static Reference of(Identifier source, TypeInfo type,
                                   boolean non_null, boolean exact) {
            return new Reference(source, type, non_null, exact);
        }

        public static Reference of(Identifier source, TypeInfo type, boolean non_null) {
            return of(source, type, non_null, false);
        }

        // type is null if exact is unknown
        public static Reference of(Identifier source, TypeId type,
                                   boolean non_null, boolean exact) {
            return of(source, TypeInfo.of(type), non_null, exact);
        }

        // type is null if exact is unknown
        public static Reference of(Identifier source, TypeId type, boolean non_null) {
            return of(source, type, non_null, false);
        }

        /* package */ boolean isNonNull() {
            return non_null;
        }

        /* package */ boolean isExact() {
            return exact;
        }

        @Override
        public String toString() {
            return toString(typeInfo().toString())
                    + (non_null ? "!" : "")
                    + (exact ? "*" : "");
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            // The exact class matters: UninitializedRef is a subclass with
            // more state, and equality must be symmetric for both kinds
            if (obj == null || obj.getClass() != getClass()) return false;
            if (!super.equals(obj)) return false;
            var other = (Reference) obj;
            return non_null == other.non_null
                    && exact == other.exact;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), non_null, exact);
        }
    }

    /**
     * A reference to an object allocated by new-instance but not yet
     * initialized by a constructor call.
     */
    public static final class UninitializedRef extends Reference {
        private final boolean thiz;

        private UninitializedRef(Identifier source, TypeId type, boolean thiz, boolean exact) {
            // Uninitialized ref type must always be known and non-null.
            // Exactness is independent of the initialization state: a
            // new-instance value is exactly its class, while the receiver
            // of a constructor may be an instance of a subclass.
            super(source, TypeInfo.of(Objects.requireNonNull(type)), true, exact);
            assert !type.isArray();
            this.thiz = thiz;
        }

        public static UninitializedRef of(Identifier source, TypeId type, boolean thiz) {
            return new UninitializedRef(source, type, thiz, false);
        }

        /**
         * @param exact whether the allocated value is exactly of the type,
         *              not of a subtype of it
         */
        public static UninitializedRef of(Identifier source, TypeId type,
                                          boolean thiz, boolean exact) {
            return new UninitializedRef(source, type, thiz, exact);
        }

        /* package */ boolean isThis() {
            return thiz;
        }

        @Override
        public String toString() {
            return "@" + super.toString() + (thiz ? ".this" : "");
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != UninitializedRef.class) return false;
            if (!super.equals(obj)) return false;
            return thiz == ((UninitializedRef) obj).thiz;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), thiz);
        }
    }

    private static ConstantKind mergeInt(ConstantKind ak, ConstantKind bk) {
        assert ak.isInt() && bk.isInt();
        if (ak.isBool() && bk.isBool()) return BOOL;
        if (ak.isPositiveByte() && bk.isPositiveByte()) return POSITIVE_BYTE;
        if (ak.isPositiveShort() && bk.isPositiveShort()) return POSITIVE_SHORT;
        if (ak.isChar() && bk.isChar()) return CHAR;
        if (ak.isPositiveInt() && bk.isPositiveInt()) return POSITIVE_INT;
        if (ak.isByte() && bk.isByte()) return BYTE;
        if (ak.isShort() && bk.isShort()) return SHORT;
        return INT;
    }

    private static ConstantKind mergeRefKinds(ConstantKind ak, ConstantKind bk) {
        assert ak.isRef() && bk.isRef();
        var a_base = ak.withoutNull();
        var b_base = bk.withoutNull();
        var nullable = ak.isNullable() || bk.isNullable();
        if (a_base == b_base) {
            return nullable ? a_base.withNull() : a_base;
        }
        // Different constant classes: the common supertype is Object
        return nullable ? REFERENCE_OR_NULL : REFERENCE;
    }

    private static TypeInfo getRefTypeInfo(ConstantKind kind) {
        class Helper {
            // Cached values
            static final TypeInfo STRING = TypeInfo.of(Ids.STRING);
            static final TypeInfo CLASS = TypeInfo.of(Ids.CLASS);
            static final TypeInfo METHOD_TYPE = TypeInfo.of(Ids.METHOD_TYPE);
            static final TypeInfo METHOD_HANDLE = TypeInfo.of(Ids.METHOD_HANDLE);
            static final TypeInfo REFERENCE = TypeInfo.of(OBJECT);
        }
        return switch (kind) {
            case STRING, STRING_OR_NULL -> Helper.STRING;
            case CLASS, CLASS_OR_NULL -> Helper.CLASS;
            case METHOD_TYPE, METHOD_TYPE_OR_NULL -> Helper.METHOD_TYPE;
            case METHOD_HANDLE, METHOD_HANDLE_OR_NULL -> Helper.METHOD_HANDLE;
            case REFERENCE, REFERENCE_OR_NULL -> Helper.REFERENCE;
            default -> throw shouldNotReachHere();
        };
    }

    /**
     * Merges two values flowing into the same register slot from
     * different predecessors; the result may be a conflict.
     */
    public static Register merge(TypeResolver resolver, int address, int slot, Register a, Register b) {
        if (Objects.equals(a, b)) return a;
        if (a.isUndefined() || b.isUndefined()) {
            return Undefined.INSTANCE;
        }
        // A joined value is identified by the merge point itself, never by
        // the instruction that happens to follow it
        var ident = new Identifier(address, slot, false);
        if (a.isConflict() || b.isConflict()) {
            return Conflict.of(ident);
        }
        if (a.isWidePiece() || b.isWidePiece()) {
            if (a.isWidePiece() && b.isWidePiece()) {
                return WidePiece.of(ident);
            }
            return Conflict.of(ident);
        }
        if (a.isUninitializedRef() || b.isUninitializedRef()) {
            // Uninitialized types are special. They may only ever be merged with
            // themselves (Equality check above). So mark any other merge as conflicting
            return Conflict.of(ident);
        }
        if (a instanceof Constant ac && b instanceof Constant bc) {
            var ak = ac.classify();
            var bk = bc.classify();
            if (ak == bk) {
                return Constant.of(ident, ak);
            }
            if (ak.isWide() || bk.isWide()) {
                if (ak.isWide() && bk.isWide()) {
                    // lo | hi
                    return WidePiece.of(ident);
                }
                return Conflict.of(ident);
            }
            assert !ak.isWide() && !bk.isWide();
            // Zero is both the integer 0 and the null reference, so it
            // adopts the other kind together with its nullability
            if (ak == ZERO) return Constant.of(ident, bk.withNull());
            if (bk == ZERO) return Constant.of(ident, ak.withNull());
            if (ak == NULL || bk == NULL) {
                // Merging null with a reference keeps that reference
                // nullable; a nullable kind keeps its nullability
                var other = ak == NULL ? bk : ak;
                if (other.isRef()) return Constant.of(ident, other.withNull());
                // Merge NULL with a primitive type
                return Conflict.of(ident);
            }
            if (ak.isRef() || bk.isRef()) {
                if (ak.isRef() && bk.isRef()) {
                    return Constant.of(ident, mergeRefKinds(ak, bk));
                }
                // Merge ref constant with a primitive type
                return Conflict.of(ident);
            }
            return Constant.of(ident, mergeInt(ak, bk));
        }
        if (a.isInt() && b.isInt()) {
            if (a.isBool() && b.isBool()) return Primitive.of(ident, TypeId.Z);
            if (a.isByte() && b.isByte()) return Primitive.of(ident, TypeId.B);
            if (a.isShort() && b.isShort()) return Primitive.of(ident, TypeId.S);
            if (a.isChar() && b.isChar()) return Primitive.of(ident, TypeId.C);
            return Primitive.of(ident, TypeId.I);
        }
        if (a.isFloat() && b.isFloat()) return Primitive.of(ident, TypeId.F);
        if (a.isLongLo() && b.isLongLo()) return WidePrimitive.of(ident, TypeId.J, true);
        if (a.isLongHi() && b.isLongHi()) return WidePrimitive.of(ident, TypeId.J, false);
        if (a.isDoubleLo() && b.isDoubleLo()) return WidePrimitive.of(ident, TypeId.D, true);
        if (a.isDoubleHi() && b.isDoubleHi()) return WidePrimitive.of(ident, TypeId.D, false);
        if (a.isRef() && b.isRef()) {
            var non_null = a.isRuntimeNonNullRef() && b.isRuntimeNonNullRef();
            var a_type = a.getTypeInfo();
            var b_type = b.getTypeInfo();
            if (a_type == null) {
                assert a.isZeroOrNull();
                return Reference.of(ident, b_type, non_null);
            }
            if (b_type == null) {
                assert b.isZeroOrNull();
                return Reference.of(ident, a_type, non_null);
            }
            assert a_type.isReference() && b_type.isReference();
            // The join is exactly typed only when both sides are exactly
            // the same class
            var exact = a.isExactRef() && b.isExactRef()
                    && Objects.equals(a_type, b_type);
            return Reference.of(ident, resolver.join(a_type, b_type), non_null, exact);
        }
        if ((a.isWideLo() && b.isWideHi()) || (a.isWideHi() && b.isWideLo())) {
            // lo | hi
            return WidePiece.of(ident);
        }
        return Conflict.of(ident);
    }

    public final boolean isUndefined() {
        return this instanceof Undefined;
    }

    public final boolean isConflict() {
        return this instanceof Conflict;
    }

    public final boolean isWidePiece() {
        return this instanceof WidePiece;
    }

    public final boolean hasContent() {
        return !isUndefined() && !isConflict() && !isWidePiece();
    }

    public final boolean isConstant() {
        return this instanceof Constant;
    }

    public final ConstantKind getConstantKind() {
        if (!(this instanceof Constant c)) {
            return null;
        }
        return c.classify();
    }

    public final boolean isZero() {
        if (!(this instanceof Constant c)) {
            return false;
        }
        var kind = c.classify();
        return kind.isZero();
    }

    public final boolean isNull() {
        if (!(this instanceof Constant c)) {
            return false;
        }
        var kind = c.classify();
        return kind.isNull();
    }

    public final boolean isZeroOrNull() {
        if (!(this instanceof Constant c)) {
            return false;
        }
        var kind = c.classify();
        return kind.isZeroOrNull();
    }

    private static boolean isBool(TypeId type) {
        return type != null && type.getShorty() == 'Z';
    }

    public final boolean isBool() {
        if (this instanceof Constant constant) {
            return constant.classify().isBool();
        }
        if (this instanceof Primitive prim) {
            TypeInfo type = prim.typeInfo();
            return !type.isArray() && isBool(type.base());
        }
        return false;
    }

    private static boolean isByte(TypeId type) {
        return type != null && switch (type.getShorty()) {
            case 'Z', 'B' -> true;
            default -> false;
        };
    }

    public final boolean isByte() {
        if (this instanceof Constant constant) {
            return constant.classify().isByte();
        }
        if (this instanceof Primitive prim) {
            TypeInfo type = prim.typeInfo();
            return !type.isArray() && isByte(type.base());
        }
        return false;
    }

    private static boolean isShort(TypeId type) {
        return type != null && switch (type.getShorty()) {
            case 'Z', 'B', 'S' -> true;
            default -> false;
        };
    }

    public final boolean isShort() {
        if (this instanceof Constant constant) {
            return constant.classify().isShort();
        }
        if (this instanceof Primitive prim) {
            TypeInfo type = prim.typeInfo();
            return !type.isArray() && isShort(type.base());
        }
        return false;
    }

    private static boolean isChar(TypeId type) {
        return type != null && switch (type.getShorty()) {
            case 'Z', 'C' -> true;
            default -> false;
        };
    }

    public final boolean isChar() {
        if (this instanceof Constant constant) {
            return constant.classify().isChar();
        }
        if (this instanceof Primitive prim) {
            TypeInfo type = prim.typeInfo();
            return !type.isArray() && isChar(type.base());
        }
        return false;
    }

    private static boolean isInt(TypeId type) {
        return type != null && switch (type.getShorty()) {
            case 'Z', 'B', 'S', 'C', 'I' -> true;
            default -> false;
        };
    }

    public final boolean isInt() {
        if (this instanceof Constant constant) {
            return constant.classify().isInt();
        }
        if (this instanceof Primitive prim) {
            TypeInfo type = prim.typeInfo();
            return !type.isArray() && isInt(type.base());
        }
        return false;
    }

    private static boolean isFloat(TypeId type) {
        // null is false
        return TypeId.F.equals(type);
    }

    public final boolean isFloat() {
        if (this instanceof Constant constant) {
            // Numeric constants are convertible to float
            return constant.classify().isInt();
        }
        if (this instanceof Primitive prim) {
            TypeInfo type = prim.typeInfo();
            return !type.isArray() && isFloat(type.base());
        }
        return false;
    }

    public final boolean isIntOrFloat() {
        if (this instanceof Constant constant) {
            return constant.classify().isInt();
        }
        return this instanceof Primitive;
    }

    public final boolean isWide() {
        if (this instanceof Constant constant) {
            return constant.classify().isWide();
        }
        return this instanceof WidePrimitive;
    }

    public final boolean isWideLo() {
        if (this instanceof Constant constant) {
            return constant.classify().isWideLo();
        }
        if (this instanceof WidePrimitive wide) {
            return wide.isLo();
        }
        return false;
    }

    public final boolean isWideHi() {
        if (this instanceof Constant constant) {
            return constant.classify().isWideHi();
        }
        if (this instanceof WidePrimitive wide) {
            return wide.isHi();
        }
        return false;
    }

    public final boolean isLong() {
        if (this instanceof Constant constant) {
            return constant.classify().isWide();
        }
        return this instanceof WidePrimitive wide
                && wide.getShorty() == 'J';
    }

    public final boolean isLongLo() {
        if (this instanceof Constant constant) {
            return constant.classify().isWideLo();
        }
        if (this instanceof WidePrimitive wide) {
            return wide.isLo() && (wide.getShorty() == 'J');
        }
        return false;
    }

    public final boolean isLongHi() {
        if (this instanceof Constant constant) {
            return constant.classify().isWideHi();
        }
        if (this instanceof WidePrimitive wide) {
            return wide.isHi() && (wide.getShorty() == 'J');
        }
        return false;
    }

    public final boolean isDouble() {
        if (this instanceof Constant constant) {
            return constant.classify().isWide();
        }
        return this instanceof WidePrimitive wide
                && wide.getShorty() == 'D';
    }

    public final boolean isDoubleLo() {
        if (this instanceof Constant constant) {
            return constant.classify().isWideLo();
        }
        if (this instanceof WidePrimitive wide) {
            return wide.isLo() && (wide.getShorty() == 'D');
        }
        return false;
    }

    public final boolean isDoubleHi() {
        if (this instanceof Constant constant) {
            return constant.classify().isWideHi();
        }
        if (this instanceof WidePrimitive wide) {
            return wide.isHi() && (wide.getShorty() == 'D');
        }
        return false;
    }

    public final boolean isRef() {
        if (this instanceof Constant constant) {
            return constant.classify().isRef();
        }
        return this instanceof Reference;
    }

    public final boolean isRuntimeNonNullRef() {
        if (this instanceof Constant constant) {
            return constant.classify().isNonZeroOrNullRef();
        }
        return this instanceof Reference ref && ref.isNonNull();
    }

    public final boolean isArray() {
        return this instanceof Reference reference
                && reference.typeInfo().isArray();
    }

    public final boolean isInitializedRef() {
        if (this instanceof Constant constant) {
            return constant.classify().isRef();
        }
        return this instanceof Reference
                && !(this instanceof UninitializedRef);
    }

    public final boolean isUninitializedRef() {
        return this instanceof UninitializedRef;
    }

    public final boolean isUninitializedThis() {
        return this instanceof UninitializedRef ref && ref.isThis();
    }

    public final TypeInfo getTypeInfo() {
        if (this instanceof TypedRegister reg) {
            return reg.typeInfo();
        }
        if (this instanceof Constant constant) {
            var kind = constant.classify();
            if (kind.isRef() && !kind.isZeroOrNull()) {
                return getRefTypeInfo(kind);
            }
        }
        return null;
    }

    /**
     * Whether every non-null value of this register is exactly of the type
     * reported by {@link #getTypeInfo()}, never of a subtype: a string,
     * class or method type constant, or a reference initialized from a
     * new-instance value. A method handle constant is not exactly typed
     * because java.lang.invoke.MethodHandle is abstract, and neither is a
     * plain reference constant.
     */
    public final boolean isExactRef() {
        if (this instanceof Constant constant) {
            return constant.classify().isExactType();
        }
        if (this instanceof Reference ref) {
            return ref.isExact();
        }
        return false;
    }

    /**
     * Checks whether this register's value is assignable to the type;
     * reference values only.
     *
     * @see TypeResolver#isAssignable(TypeInfo, TypeId, boolean, boolean)
     */
    public final boolean isAssignable(TypeResolver resolver, TypeId type,
                                      boolean allowUninitialized, boolean unknownMatches, boolean strict) {
        Objects.requireNonNull(type);
        if (!type.isReference()) {
            return false;
        }
        if (this instanceof Constant constant) {
            var kind = constant.classify();
            if (kind.isZeroOrNull()) {
                // All reference types can be assigned null
                return true;
            }
            if (kind.isRef()) {
                var info = getRefTypeInfo(kind);
                return resolver.isAssignable(info, type, unknownMatches, strict);
            }
            return false;
        }
        if (this instanceof Reference ref) {
            if (!allowUninitialized && ref instanceof UninitializedRef) {
                return false;
            }
            return resolver.isAssignable(ref.typeInfo(), type, unknownMatches, strict);
        }
        return false;
    }

    /**
     * @see #isAssignable(TypeResolver, TypeId, boolean, boolean, boolean)
     */
    public final boolean isAssignable(TypeResolver resolver, TypeId type,
                                      boolean allowUninitialized, boolean unknownMatches) {
        return isAssignable(resolver, type, allowUninitialized, unknownMatches, true);
    }
}
