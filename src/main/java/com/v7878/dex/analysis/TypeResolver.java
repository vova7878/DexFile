package com.v7878.dex.analysis;

import static com.v7878.dex.immutable.TypeId.OBJECT;
import static com.v7878.dex.util.Ids.CLONEABLE;
import static com.v7878.dex.util.Ids.SERIALIZABLE;

import com.v7878.dex.immutable.TypeId;

import java.util.Objects;

public abstract class TypeResolver {
    // Returns "I don't know" for all requests
    public static final TypeResolver DEFAULT = new TypeResolver() {
        @Override
        public TypeId join(TypeId a, TypeId b) {
            return null;
        }

        @Override
        public Boolean instanceOf(TypeId a, TypeId b) {
            return null;
        }

        @Override
        public Boolean isInterface(TypeId type) {
            return null;
        }
    };

    public abstract TypeId join(TypeId a, TypeId b);

    public abstract Boolean instanceOf(TypeId a, TypeId b);

    public abstract Boolean isInterface(TypeId type);

    private static boolean instanceOfFlat(TypeResolver resolver, TypeId a,
                                          TypeId b, boolean default_value, boolean strict) {
        if (OBJECT.equals(b)) {
            return true;
        }
        if (strict && OBJECT.equals(a)) {
            // There is nothing above Object
            return false;
        }
        if (a == null || b == null) {
            return default_value;
        }
        if (Objects.equals(a, b)) {
            return true;
        }
        if (!strict) {
            if (_isInterface(resolver, b, default_value)) {
                return true;
            }
            if (OBJECT.equals(a)) {
                // There is nothing above Object
                return false;
            }
        }
        var out = resolver.instanceOf(a, b);
        return out == null ? default_value : out;
    }

    /* package */
    static boolean _instanceOf(TypeResolver resolver, TypeInfo a,
                               TypeInfo b, boolean default_value, boolean strict) {
        if (a.isPrimitive() || b.isPrimitive()) {
            throw new IllegalArgumentException(
                    "The argument can only be a reference type");
        }
        if (Objects.equals(a, b)) {
            var unresolved = a.isUnresolved();
            assert unresolved == b.isUnresolved();
            return !unresolved || default_value;
        }
        if (b.array_depth() > a.array_depth()) return false;
        if (b.array_depth() < a.array_depth()) {
            if (!strict && b.base() != null) {
                if (_isInterface(resolver, b.base(), default_value)) {
                    return true;
                }
            }
            // T[][] instanceof ?[] or
            // T[][] instanceof any of Object[], Serializable[] or Cloneable[]
            return b.base() == null ? default_value : (OBJECT.equals(b.base()) ||
                    SERIALIZABLE.equals(b.base()) || CLONEABLE.equals(b.base()));
        }
        if (a.isBasePrimitive() || b.isBasePrimitive()) {
            // Different primitive bases
            // int[]...[] instanceof float[]...[]
            // or ref[]...[] instanceof int[]...[]
            // or int[]...[] instanceof ref[]...[]
            return false;
        }
        return instanceOfFlat(resolver, a.base(), b.base(), default_value, strict);
    }

    /* package */
    static boolean _instanceOf(TypeResolver resolver, TypeInfo a,
                               TypeId b, boolean default_value, boolean strict) {
        return _instanceOf(resolver, a, TypeInfo.of(b), default_value, strict);
    }

    /* package */
    @SuppressWarnings("SameParameterValue")
    static boolean _instanceOf(TypeResolver resolver, TypeId a,
                               TypeInfo b, boolean default_value, boolean strict) {
        return _instanceOf(resolver, TypeInfo.of(a), b, default_value, strict);
    }

    /* package */
    @SuppressWarnings("SameParameterValue")
    static boolean _instanceOf(TypeResolver resolver, TypeId a,
                               TypeId b, boolean default_value, boolean strict) {
        return _instanceOf(resolver, TypeInfo.of(a), b, default_value, strict);
    }

    private static TypeId joinFlat(TypeResolver resolver, TypeId a, TypeId b) {
        if (Objects.equals(a, b)) {
            return a;
        }
        if (OBJECT.equals(a) || OBJECT.equals(b)) {
            return OBJECT;
        }
        if (a == null || b == null) {
            return null;
        }
        return resolver.join(a, b);
    }

    /* package */
    static TypeInfo _join(TypeResolver resolver, TypeInfo a, TypeInfo b) {
        if (a.isPrimitive() || b.isPrimitive()) {
            throw new IllegalArgumentException(
                    "The argument can only be a reference type");
        }
        if (Objects.equals(a, b)) {
            return a;
        }
        int depth = Math.min(a.array_depth(), b.array_depth());
        if ((a.array_depth() == depth && a.isBasePrimitive())
                || (b.array_depth() == depth && b.isBasePrimitive())) {
            depth--;
        }
        assert depth >= 0;
        if (a.array_depth() != depth || b.array_depth() != depth) {
            // Note: All arrays implement Serializable and Cloneable
            if (a.array_depth() == depth && (a.base() == null ||
                    SERIALIZABLE.equals(a.base()) || CLONEABLE.equals(a.base()))) {
                return a;
            }
            if (b.array_depth() == depth && (b.base() == null ||
                    SERIALIZABLE.equals(b.base()) || CLONEABLE.equals(b.base()))) {
                return b;
            }
            return new TypeInfo(OBJECT, depth);
        }
        return new TypeInfo(joinFlat(resolver, a.base(), b.base()), depth);
    }

    /* package */
    static TypeInfo _join(TypeResolver resolver, TypeInfo a, TypeId b) {
        return _join(resolver, a, TypeInfo.of(b));
    }

    /* package */
    static boolean _isInterface(TypeResolver resolver, TypeId type, boolean default_value) {
        if (type.isPrimitive()) {
            throw new IllegalArgumentException(
                    "The argument can only be a reference type");
        }
        if (type.isArray()) {
            return false;
        }
        var out = resolver.isInterface(type);
        return out == null ? default_value : out;
    }
}
