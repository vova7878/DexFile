package com.v7878.dex.reader.util;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public abstract class FixedSizeSet<T> extends AbstractSet<T> {
    private final int size;

    public FixedSizeSet(int size) {
        this.size = size;
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
                return compute(index++);
            }
        };
    }

    protected abstract T compute(int index);

    public static <E> FixedSizeSet<E> ofList(List<E> list) {
        return new FixedSizeSet<>(list.size()) {
            @Override
            protected E compute(int index) {
                return list.get(index);
            }
        };
    }
}
