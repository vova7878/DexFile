package com.v7878.dex.util;

import com.v7878.dex.Mutable;

import java.util.Objects;

public class MutableSparseArray<T extends Mutable> extends SparseArray<T> implements Mutable {
    public MutableSparseArray() {
        this(0);
    }

    public MutableSparseArray(int initialCapacity) {
        super(initialCapacity);
    }

    public static <E extends Mutable> MutableSparseArray<E> empty() {
        return new MutableSparseArray<>();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    protected T check(T element) {
        return Objects.requireNonNull(element,
                "MutableList can`t contain null element");
    }

    @Override
    @SuppressWarnings("unchecked")
    public void put(int key, T value) {
        super.put(key, (T) check(value).mutate());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setValueAt(int index, T value) {
        super.setValueAt(index, (T) check(value).mutate());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MutableSparseArray) {
            MutableSparseArray<?> other = (MutableSparseArray<?>) obj;
            return contentEquals(other);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return contentHashCode();
    }

    @Override
    public MutableSparseArray<T> mutate() {
        //TODO: maybe faster?
        int count = size();
        MutableSparseArray<T> out = new MutableSparseArray<>(count);
        for (int i = 0; i < count; i++) {
            out.put(keyAt(i), valueAt(i));
        }
        return out;
    }
}
