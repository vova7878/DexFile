package com.v7878.dex.util;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public abstract class ImmutableAbstractList<T> extends AbstractList<T> {
    private final int size;

    public ImmutableAbstractList(int size) {
        this.size = size;
    }

    @Override
    public int size() {
        return size;
    }

    /**
     * The {@code List} contract, walked by index: both lists must
     * have the same size and equal elements at every index. No
     * iterator is created for either list.
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof List<?> list)) {
            return false;
        }
        int n = size;
        if (list.size() != n) {
            return false;
        }
        for (int i = 0; i < n; i++) {
            if (!Objects.equals(get(i), list.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * The {@code List} hash formula, walked by index: {@code h = 1},
     * then {@code h = 31 * h + Objects.hashCode(get(i))} per element.
     */
    @Override
    public int hashCode() {
        int hash = 1;
        for (int i = 0; i < size; i++) {
            hash = 31 * hash + Objects.hashCode(get(i));
        }
        return hash;
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
}
