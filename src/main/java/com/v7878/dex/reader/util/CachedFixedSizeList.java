package com.v7878.dex.reader.util;

import java.util.AbstractList;

public abstract class CachedFixedSizeList<T> extends AbstractList<T> {
    private final T[] data;
    private final int size;

    public CachedFixedSizeList(int size) {
        this.size = size;
        //noinspection unchecked
        this.data = (T[]) new Object[size];
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public T get(int index) {
        T value = data[index];
        if (value != null) return value;
        return data[index] = compute(index);
    }

    protected abstract T compute(int index);
}
