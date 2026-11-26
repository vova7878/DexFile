package com.v7878.dex.util;

import java.util.Objects;

public abstract class CachedFixedSizeList<T> extends ImmutableAbstractList<T> {
    private T[] data;

    public CachedFixedSizeList(int size) {
        super(size);
    }

    @SuppressWarnings("unchecked")
    private T[] getCache() {
        T[] arr = data;
        if (arr == null) {
            data = arr = (T[]) new Object[size()];
        }
        return arr;
    }

    @Override
    public T get(int index) {
        Objects.checkIndex(index, size());
        T[] cache = getCache();
        T value = cache[index];
        if (value != null) return value;
        return cache[index] = Objects.requireNonNull(compute(index));
    }

    protected abstract T compute(int index);
}
