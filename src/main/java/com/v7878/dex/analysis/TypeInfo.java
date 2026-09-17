package com.v7878.dex.analysis;

import com.v7878.dex.immutable.TypeId;

/**
 * A type with a known array depth: the non-array base type plus the
 * number of array dimensions around it. A null base means the exact
 * reference type is unresolved.
 *
 * @param base        the non-array base type, or null if unknown
 * @param array_depth the number of array dimensions
 */
public record TypeInfo(TypeId base, int array_depth) {
    public TypeInfo {
        if (base != null && base.isArray()) {
            throw new IllegalArgumentException("Illegal base " + base);
        }
        if (array_depth < 0) {
            throw new IllegalArgumentException("Negative array depth");
        }
    }

    /**
     * Splits the type into its base and array depth; null stays
     * unresolved.
     */
    public static TypeInfo of(TypeId type) {
        if (type == null) return new TypeInfo(null, 0);
        return new TypeInfo(type.baseType(), type.getArrayDepth());
    }

    /**
     * The full array type, or null if unresolved.
     */
    public TypeId exactType() {
        return base == null ? null : base.array(array_depth);
    }

    public boolean isUnresolved() {
        return base == null;
    }

    public boolean isArray() {
        return array_depth > 0;
    }

    public boolean isBaseReference() {
        return base == null || base.isReference();
    }

    public boolean isBasePrimitive() {
        return !isBaseReference();
    }

    public char getBaseShorty() {
        if (base == null) return 'L';
        return base.getShorty();
    }

    private void checkArray() {
        if (!isArray()) {
            throw new IllegalStateException(this + " is not array");
        }
    }

    /**
     * Whether the array component is a reference.
     *
     * @throws IllegalStateException if this is not an array
     */
    public boolean isComponentReference() {
        checkArray();
        return array_depth > 1 || isBaseReference();
    }

    public boolean isComponentPrimitive() {
        return !isComponentReference();
    }

    public char getComponentShorty() {
        checkArray();
        if (base == null) return 'L';
        if (array_depth > 1) return 'L';
        return base.getShorty();
    }

    /**
     * The type of the array component (one dimension less).
     *
     * @throws IllegalStateException if this is not an array
     */
    public TypeInfo getComponentType() {
        checkArray();
        return new TypeInfo(base, array_depth - 1);
    }

    public boolean isReference() {
        return isArray() || isBaseReference();
    }

    public boolean isPrimitive() {
        return !isReference();
    }

    public char getShorty() {
        if (isArray() || base == null) return 'L';
        return base.getShorty();
    }

    @Override
    public String toString() {
        return (base == null ? "<unresolved reference>" : base.getName()) + "[]".repeat(array_depth);
    }
}
