package com.v7878.dex.analysis;

import static com.v7878.dex.immutable.TypeId.OBJECT;

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
    };

    public abstract TypeId join(TypeId a, TypeId b);

    public abstract Boolean instanceOf(TypeId a, TypeId b);

    // TODO: public abstract Boolean isInterface(TypeId type);

    /* package */
    private static boolean instanceOfFlat(TypeResolver resolver, TypeId a,
                                          TypeId b, boolean default_value) {
        if (OBJECT.equals(b)) {
            return true;
        }
        if (a == null || b == null) {
            return default_value;
        }
        if (Objects.equals(a, b)) {
            return true;
        }
        var out = resolver.instanceOf(a, b);
        return out == null ? default_value : out;
    }

    /* package */
    static boolean _instanceOf(TypeResolver resolver, TypeInfo a,
                               TypeInfo b, boolean default_value) {
        if (a.isPrimitive() || b.isPrimitive()) {
            throw new IllegalArgumentException(
                    "The argument can only be a reference type");
        }
        if (Objects.equals(a, b)) {
            var unresolved = a.isUnresolved();
            assert unresolved == b.isUnresolved();
            return !unresolved || default_value;
        }
        int depth = a.array_depth();
        if (b.array_depth() < depth) {
            // T[][] instanceof ?[] or T[][] instanceof Object[]
            return b.base() == null ? default_value : OBJECT.equals(b.base());
        }
        if (b.array_depth() > depth) return false;
        if (a.isBasePrimitive() || b.isBasePrimitive()) {
            // Different primitive bases
            // int[]...[] instanceof float[]...[]
            return false;
        }
        return instanceOfFlat(resolver, a.base(), b.base(), default_value);
    }

    /* package */
    static boolean _instanceOf(TypeResolver resolver, TypeInfo a,
                               TypeId b, boolean default_value) {
        return _instanceOf(resolver, a, TypeInfo.of(b), default_value);
    }

    /* package */
    @SuppressWarnings("SameParameterValue")
    static boolean _instanceOf(TypeResolver resolver, TypeId a,
                               TypeId b, boolean default_value) {
        return _instanceOf(resolver, TypeInfo.of(a), b, default_value);
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
        if (depth < 0) {
            throw new IllegalArgumentException("Conflicting types: " + a + " and " + b);
        }
        if (a.array_depth() != depth || b.array_depth() != depth) {
            return new TypeInfo(OBJECT, depth);
        }
        return new TypeInfo(joinFlat(resolver, a.base(), b.base()), depth);
    }

    /* package */
    static TypeInfo _join(TypeResolver resolver, TypeInfo a, TypeId b) {
        return _join(resolver, a, TypeInfo.of(b));
    }
}
