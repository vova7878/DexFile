package com.v7878.dex.reader.util;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class CachedVariableSizeSet<T> extends AbstractSet<T> {
    private final T[] data;
    private final int size;
    private int position;

    public CachedVariableSizeSet(int size) {
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

    public void computeAll() {
        get(Math.max(0, size - 1));
    }

    protected abstract T computeNext();
}
