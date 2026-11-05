package com.v7878.dex.util;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;

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

    @SuppressWarnings("unchecked")
    private T[] getCache() {
        T[] arr = data;
        if (arr == null) {
            data = arr = (T[]) new Object[size];
        }
        return arr;
    }

    @Override
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(
                    "Index: " + index + ", Size: " + size);
        }
        T[] cache = getCache();
        T value = cache[index];
        if (value != null) return value;
        return cache[index] = Objects.requireNonNull(compute(index));
    }

    @Override
    public Iterator<T> iterator() {
        return listIterator(0);
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException(
                    "Index: " + index + ", Size: " + size);
        }
        return new ListItr(index);
    }

    /**
     * Unlike the iterators of {@code AbstractList}, {@code next()} does not
     * translate an {@code IndexOutOfBoundsException} thrown by {@link #get}
     * into a {@code NoSuchElementException}: a failed element computation
     * must surface as the original error, not as the end of the list.
     */
    private class ListItr implements ListIterator<T> {
        private int cursor;

        ListItr(int index) {
            cursor = index;
        }

        @Override
        public boolean hasNext() {
            return cursor < size;
        }

        @Override
        public boolean hasPrevious() {
            return cursor > 0;
        }

        @Override
        public T next() {
            if (cursor >= size) {
                throw new NoSuchElementException();
            }
            return get(cursor++);
        }

        @Override
        public T previous() {
            if (cursor <= 0) {
                throw new NoSuchElementException();
            }
            return get(--cursor);
        }

        @Override
        public int nextIndex() {
            return cursor;
        }

        @Override
        public int previousIndex() {
            return cursor - 1;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(T t) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(T t) {
            throw new UnsupportedOperationException();
        }
    }

    protected abstract T compute(int index);
}
