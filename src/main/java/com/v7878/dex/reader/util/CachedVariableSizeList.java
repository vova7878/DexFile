package com.v7878.dex.reader.util;

import java.util.AbstractList;

public abstract class CachedVariableSizeList<T> extends AbstractList<T> {
    private final int size;
    private T[] data;
    private int position;

    public CachedVariableSizeList(int size) {
        this.size = size;
        this.position = 0;
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
        return compute(index);
    }

    private T compute(int index) {
        T[] cache = getCache();
        while (position <= index) {
            cache[position++] = computeNext();
        }
        return cache[index];
    }

    public void computeAll() {
        get(Math.max(0, size - 1));
    }

    protected abstract T computeNext();
}
