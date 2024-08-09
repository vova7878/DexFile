package com.v7878.dex.reader.util;

import java.util.AbstractList;

public abstract class CachedVariableSizeList<T> extends AbstractList<T> {
    private final T[] data;
    private final int size;
    private int position;

    public CachedVariableSizeList(int size) {
        this.size = size;
        this.position = 0;
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
        return compute(index);
    }

    private T compute(int index) {
        while (position <= index) {
            data[position++] = computeNext();
        }
        return data[index];
    }

    protected abstract T computeNext();
}
