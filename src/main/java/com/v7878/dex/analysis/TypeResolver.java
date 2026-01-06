package com.v7878.dex.analysis;

import static com.v7878.dex.immutable.TypeId.OBJECT;

import com.v7878.dex.immutable.TypeId;

import java.util.Objects;

public abstract class TypeResolver {
    public static final TypeResolver DEFAULT = new TypeResolver() {
        @Override
        public TypeId join(TypeId a, TypeId b) {
            return null;
        }

        @Override
        public boolean instanceOf(TypeId a, TypeId b) {
            return true;
        }
    };

    public abstract TypeId join(TypeId a, TypeId b);

    public abstract boolean instanceOf(TypeId a, TypeId b);

    /* package */
    static boolean _instanceOf(TypeId a, TypeId b) {
        // TODO
        return DEFAULT.instanceOf(a, b);
    }

    /* package */
    static TypeId joinFlat(TypeId a, TypeId b) {
        if (OBJECT.equals(a) || OBJECT.equals(b)) {
            return OBJECT;
        }
        if (a == null || b == null) {
            return null;
        }
        // TODO
        return DEFAULT.join(a, b);
    }

    /* package */
    static TypeInfo _join(TypeInfo a, TypeInfo b) {
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
        return new TypeInfo(joinFlat(a.base(), b.base()), depth);
    }
}
