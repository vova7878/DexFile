package com.v7878.dex.reader.util;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class CachedVariableSizeSet<T> extends AbstractSet<T> {
    private final int size;
    private T[] data;
    private int position;

    public CachedVariableSizeSet(int size) {
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
    public Iterator<T> iterator() {
        return new Iterator<>() {
            int index = 0;

            @Override
            public boolean hasNext() {
                return index < size();
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return get(index++);
            }
        };
    }

    private T get(int index) {
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
