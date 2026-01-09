package com.v7878.dex.analysis;

import static com.v7878.dex.analysis.Register.ConstantKind.BOOL;
import static com.v7878.dex.analysis.Register.ConstantKind.BYTE;
import static com.v7878.dex.analysis.Register.ConstantKind.CHAR;
import static com.v7878.dex.analysis.Register.ConstantKind.INT;
import static com.v7878.dex.analysis.Register.ConstantKind.NULL;
import static com.v7878.dex.analysis.Register.ConstantKind.POSITIVE_BYTE;
import static com.v7878.dex.analysis.Register.ConstantKind.POSITIVE_SHORT;
import static com.v7878.dex.analysis.Register.ConstantKind.SHORT;
import static com.v7878.dex.analysis.Register.ConstantKind.WIDE_HI;
import static com.v7878.dex.analysis.Register.ConstantKind.WIDE_LO;
import static com.v7878.dex.analysis.Register.ConstantKind.ZERO;
import static com.v7878.dex.util.Checks.shouldNotReachHere;
import static com.v7878.dex.util.ShortyUtils.invalidShorty;

import com.v7878.dex.immutable.TypeId;
import com.v7878.dex.util.Ids;

import java.util.Objects;

public sealed abstract class Register {
    public record Identifier(int address, int slot) {
        public Identifier {
            if (address != -1 && address < 0) {
                throw new IllegalArgumentException(
                        "Invalid address: " + address);
            }
        }

        public boolean isParameter() {
            return address == -1;
        }

        @Override
        public String toString() {
            return (address < 0 ? "parameter" : Integer.toHexString(address)) + "[" + slot + "]";
        }
    }

    public abstract Identifier getSource();

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

    // Local registers in which nothing was stored yet
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

    // A piece of broken wide pair
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
        // Constant [-0x80 - -1]
        // Merged [-0x80 - 0x7f]
        BYTE,
        // Constant [-0x8000 - -0x81]
        // Merged [-0x8000 - 0x7fff]
        SHORT,
        // Constant [-0x80000000 - -0x8001] | [0x10000 - 0x7fffffff]
        // Merged [-0x80000000 - 0x7fffffff]
        INT,
        WIDE_LO,
        WIDE_HI,
        STRING,
        CLASS,
        METHOD_TYPE,
        METHOD_HANDLE,
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

        public boolean isByte() {
            return isPositiveByte()
                    || this == BYTE;
        }

        public boolean isPositiveShort() {
            return isPositiveByte()
                    || this == POSITIVE_SHORT;
        }

        public boolean isShort() {
            return isByte()
                    || this == POSITIVE_SHORT
                    || this == SHORT;
        }

        public boolean isChar() {
            return isBool()
                    || this == POSITIVE_BYTE
                    || this == POSITIVE_SHORT
                    || this == CHAR;
        }

        public boolean isInt() {
            return isShort()
                    || this == CHAR
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
            return this == STRING;
        }

        public boolean isClass() {
            return this == CLASS;
        }

        public boolean isMethodType() {
            return this == METHOD_TYPE;
        }

        public boolean isMethodHandle() {
            return this == METHOD_HANDLE;
        }

        public boolean isNonZeroOrNullRef() {
            return isString()
                    || isClass()
                    || isMethodType()
                    || isMethodHandle();
        }

        public boolean isNull() {
            return this == NULL;
        }

        public boolean isZeroOrNull() {
            return isZero() || isNull();
        }

        public boolean isRef() {
            return isZeroOrNull()
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
                case BYTE -> "byte";
                case SHORT -> "short";
                case INT -> "int";
                case WIDE_LO -> "<wide";
                case WIDE_HI -> ">wide";
                case STRING -> "string";
                case CLASS -> "class";
                case METHOD_TYPE -> "method type";
                case METHOD_HANDLE -> "method handle";
                case NULL -> "null";
            };
        }
    }

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
        } else if (value >= -0x80 && value <= -1) {
            return BYTE;
        } else if (value >= -0x8000 && value <= -0x81) {
            return SHORT;
        }
        return INT;
    }

    public static abstract sealed class TypedRegister extends DynamicRegister {
        private final TypeInfo type_info;

        private TypedRegister(Identifier source, TypeInfo type_info) {
            super(source);
            this.type_info = Objects.requireNonNull(type_info);
        }

        public TypeInfo typeInfo() {
            return type_info;
        }

        public char getShorty() {
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

        public ConstantKind classify() {
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

        public boolean isLo() {
            return lo;
        }

        public boolean isHi() {
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

    // type is null if exact is unknown
    public static sealed class Reference extends TypedRegister {
        private Reference(Identifier source, TypeInfo type) {
            super(source, type);
            if (type.isPrimitive()) {
                throw new IllegalArgumentException(
                        type + " must not be primitive");
            }
        }

        public static Reference of(Identifier source, TypeInfo type) {
            return new Reference(source, type);
        }

        public static Reference of(Identifier source, TypeId type) {
            return of(source, TypeInfo.of(type));
        }

        @Override
        public String toString() {
            return toString(typeInfo().toString());
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (!super.equals(obj)) return false;
            return obj instanceof Reference;
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }
    }

    public static final class UninitializedRef extends Reference {
        private final boolean thiz;

        private UninitializedRef(Identifier source, TypeId type, boolean thiz) {
            // Uninitialized ref type must always be known
            super(source, TypeInfo.of(Objects.requireNonNull(type)));
            assert !type.isArray();
            this.thiz = thiz;
        }

        public static UninitializedRef of(Identifier source, TypeId type, boolean thiz) {
            return new UninitializedRef(source, type, thiz);
        }

        public boolean isThis() {
            return thiz;
        }

        @Override
        public String toString() {
            return "@" + super.toString() + (thiz ? ".this" : "");
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (!super.equals(obj)) return false;
            return obj instanceof UninitializedRef other
                    && thiz == other.thiz;
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
        if (ak.isByte() && bk.isByte()) return BYTE;
        if (ak.isShort() && bk.isShort()) return SHORT;
        return INT;
    }

    private static TypeInfo getRefTypeInfo(ConstantKind kind) {
        class Helper {
            // Cached values
            static final TypeInfo STRING = TypeInfo.of(Ids.STRING);
            static final TypeInfo CLASS = TypeInfo.of(Ids.CLASS);
            static final TypeInfo METHOD_TYPE = TypeInfo.of(Ids.METHOD_TYPE);
            static final TypeInfo METHOD_HANDLE = TypeInfo.of(Ids.METHOD_HANDLE);
        }
        return switch (kind) {
            case STRING -> Helper.STRING;
            case CLASS -> Helper.CLASS;
            case METHOD_TYPE -> Helper.METHOD_TYPE;
            case METHOD_HANDLE -> Helper.METHOD_HANDLE;
            default -> throw shouldNotReachHere();
        };
    }

    public static Register merge(TypeResolver resolver, int address, int slot, Register a, Register b) {
        if (Objects.equals(a, b)) return a;
        if (a.isUndefined() || b.isUndefined()) {
            return Undefined.INSTANCE;
        }
        var ident = new Identifier(address, slot);
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
            // Unitialized types are special. They may only ever be merged with
            // themselves (Equality check above). So mark any other merge as conflicting
            return Conflict.of(ident);
        }
        if (a instanceof Constant ac && b instanceof Constant bc) {
            var ak = ac.classify();
            var bk = bc.classify();
            if (ak.isWide() || bk.isWide()) {
                if (ak == bk) {
                    return Constant.of(ident, ak);
                }
                return Conflict.of(ident);
            }
            assert !ak.isWide() && !bk.isWide();
            if (ak == bk) {
                return Constant.of(ident, ak);
            }
            if (ak == ZERO) return Constant.of(ident, bk);
            if (bk == ZERO) return Constant.of(ident, ak);
            if (ak == NULL || bk == NULL) {
                if (ak.isNonZeroOrNullRef()) return Constant.of(ident, ak);
                if (bk.isNonZeroOrNullRef()) return Constant.of(ident, bk);
                // Merge NULL with a primitive type
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
            var a_type = a.getTypeInfo();
            var b_type = b.getTypeInfo();
            if (a_type == null) {
                assert a.isZeroOrNull();
                return Reference.of(ident, b_type);
            }
            if (b_type == null) {
                assert b.isZeroOrNull();
                return Reference.of(ident, a_type);
            }
            assert a_type.isReference() && b_type.isReference();
            return Reference.of(ident, TypeResolver._join(resolver, a_type, b_type));
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

    public final boolean isConstant() {
        return this instanceof Constant;
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

    public final boolean isArray() {
        return this instanceof Reference reference
                && reference.typeInfo().isArray();
    }

    public final TypeInfo getTypeInfo() {
        if (this instanceof TypedRegister reg) {
            return reg.typeInfo();
        }
        if (this instanceof Constant constant) {
            var kind = constant.classify();
            if (kind.isNonZeroOrNullRef()) {
                return getRefTypeInfo(kind);
            }
        }
        return null;
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

    // Note: only for ref types
    public final boolean instanceOf(TypeResolver resolver, TypeId type, boolean allow_uninitialized) {
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
            if (kind.isNonZeroOrNullRef()) {
                var info = getRefTypeInfo(kind);
                return TypeResolver._instanceOf(resolver, info, type);
            }
            return false;
        }
        if (this instanceof Reference ref) {
            if (!allow_uninitialized && ref instanceof UninitializedRef) {
                return false;
            }
            return TypeResolver._instanceOf(resolver, ref.typeInfo(), type);
        }
        return false;
    }
}
