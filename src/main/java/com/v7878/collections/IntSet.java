package com.v7878.collections;

import com.v7878.dex.util.EmptyArrays;

import java.util.Arrays;
import java.util.Objects;

public final class IntSet {
    private boolean ro;
    private int[] array;
    private int size;

    public IntSet() {
        this(0);
    }

    public IntSet(int initialCapacity) {
        this.array = initialCapacity <= 0 ? EmptyArrays.INT : new int[initialCapacity];
        this.size = 0;
        this.ro = false;
    }

    public IntSet(IntSet other) {
        var length = other.size;
        this.size = length;
        if (length == 0) {
            this.array = EmptyArrays.INT;
        } else {
            this.array = other.array.clone();
        }
        this.ro = false;
    }

    IntSet(IntSet other, boolean ro) {
        var length = other.size;
        this.size = length;
        if (length == 0) {
            this.array = EmptyArrays.INT;
        } else {
            if (ro) {
                this.array = other.array;
                if (!trimToSize()) {
                    this.array = this.array.clone();
                }
            } else {
                this.array = other.array.clone();
            }
        }
        this.ro = ro;
    }

    IntSet(int[] array, int size, boolean ro) {
        this.array = array;
        this.size = size;
        this.ro = ro;
    }

    private static final IntSet EMPTY = new IntSet(
            EmptyArrays.INT, 0, true);

    public static IntSet empty() {
        return EMPTY;
    }

    public IntSet duplicate() {
        return new IntSet(this, ro);
    }

    public IntSet freeze() {
        if (ro) {
            return this;
        }
        trimToSize();
        ro = true;
        return this;
    }

    private void checkWritable() {
        if (ro) {
            throw new UnsupportedOperationException();
        }
    }

    public void ensureCapacity(int minCapacity) {
        checkWritable();
        if (minCapacity > array.length) {
            array = Arrays.copyOf(array, minCapacity);
        }
    }

    public int indexOf(int value) {
        return ArraySupport.binarySearch(array, 0, size, value);
    }

    public boolean contains(int value) {
        return indexOf(value) >= 0;
    }

    private void removeAtRaw(int index) {
        int from = index + 1;
        if (from < size) {
            System.arraycopy(array, from, array, index, size - from);
        }
        size--;
    }

    /**
     * @return {@code true} if this set contained the specified element
     */
    public boolean remove(int value) {
        checkWritable();
        int i = indexOf(value);
        if (i >= 0) {
            removeAtRaw(i);
            return true;
        }
        return false;
    }

    public void removeAt(int index) {
        checkWritable();
        Objects.checkIndex(index, size);
        removeAtRaw(index);
    }

    public void removeAtRange(int index, int length) {
        checkWritable();
        Objects.checkFromIndexSize(index, length, size);
        if (length <= 0) {
            return;
        }
        int from = index + length;
        if (from < size) {
            System.arraycopy(array, from, array, index, size - from);
        }
        size -= length;
    }

    /**
     * @return {@code true} if this set did not already contain the specified element
     */
    public boolean add(int value) {
        checkWritable();
        int i = indexOf(value);
        if (i >= 0) {
            return false;
        }
        i = ~i;
        array = ArraySupport.insert(array, size, i, value);
        size++;
        return true;
    }

    public void addAll(IntSet other) {
        checkWritable();
        Objects.requireNonNull(other);
        int length = other.size;
        if (length == 0) {
            return;
        }
        ensureCapacity(size + length);
        int[] other_array = other.array;
        for (int i = 0; i < length; i++) {
            add(other_array[i]);
        }
    }

    public void addAll(int[] other) {
        checkWritable();
        Objects.requireNonNull(other);
        int length = other.length;
        if (length == 0) {
            return;
        }
        ensureCapacity(size + length);
        for (int v : other) {
            add(v);
        }
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int at(int index) {
        Objects.checkIndex(index, size);
        return array[index];
    }

    public int first() {
        return at(0);
    }

    public int last() {
        return at(size - 1);
    }

    public int[] toArray() {
        return Arrays.copyOf(array, size);
    }

    public void clear() {
        checkWritable();
        size = 0;
    }

    @Override
    public String toString() {
        int length = size;
        if (length <= 0) {
            return "{}";
        }
        StringBuilder buffer = new StringBuilder(length * 12);
        buffer.append('{');
        for (int i = 0; i < length; i++) {
            if (i > 0) {
                buffer.append(", ");
            }
            int key = at(i);
            buffer.append(key);
        }
        buffer.append('}');
        return buffer.toString();
    }

    public boolean contentEquals(IntSet other) {
        if (other == null) {
            return false;
        }
        int length = size;
        if (length != other.size) {
            return false;
        }
        return Arrays.equals(array, 0, size,
                other.array, 0, other.size);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return obj instanceof IntSet other
                && contentEquals(other);
    }

    public int contentHashCode() {
        int hash = 0;
        int length = size;
        for (int index = 0; index < length; index++) {
            int value = array[index];
            hash = 31 * hash + value;
        }
        return hash;
    }

    @Override
    public int hashCode() {
        return contentHashCode();
    }

    public boolean trimToSize() {
        checkWritable();
        int length = size;
        if (array.length > length) {
            array = Arrays.copyOf(array, length);
            return true;
        }
        return false;
    }
}
