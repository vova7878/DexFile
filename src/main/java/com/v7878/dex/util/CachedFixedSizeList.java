package com.v7878.dex.util;

import java.util.AbstractList;

public abstract class CachedFixedSizeList<T> extends AbstractList<T> {
    private final int size;
    private T[] data;

    public CachedFixedSizeList(int size) {
        this.size = size;
    }

    @Override
    public int size() {
        return size;
    }

    private T[] getCache() {
        T[] arr = data;
        if (arr == null) {
            //noinspection unchecked
            data = arr = (T[]) new Object[size];
        }
        return arr;
    }

    @Override
    public T get(int index) {
        T[] cache = getCache();
        T value = cache[index];
        if (value != null) return value;
        return cache[index] = compute(index);
    }

    protected abstract T compute(int index);
}
