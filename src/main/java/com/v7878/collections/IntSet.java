package com.v7878.collections;

import com.v7878.dex.util.EmptyArrays;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.IntStream;

public final class IntSet implements Iterable<Integer> {
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

    /**
     * A frozen set returns itself; a mutable set returns a writable copy.
     */
    public IntSet duplicate() {
        return ro ? this : new IntSet(this, false);
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

    /**
     * Removes every value in {@code [from_value, to_value)}.
     * An empty or inverted range removes nothing.
     *
     * @return {@code true} if at least one value was removed
     */
    public boolean removeRange(int from_value, int to_value) {
        checkWritable();
        if (to_value <= from_value) {
            return false;
        }
        // each search result is the first index of a value >= the bound
        int from = indexOf(from_value);
        from = from >= 0 ? from : ~from;
        int to = indexOf(to_value);
        to = to >= 0 ? to : ~to;
        if (from >= to) {
            return false;
        }
        if (to < size) {
            // The roles are swapped on purpose: 'to' is the source and
            // 'from' the destination of this copy
            System.arraycopy(array, to, array, from, size - to);
        }
        size -= to - from;
        return true;
    }

    /**
     * @return {@code true} if at least one value was removed
     */
    public boolean removeAtRange(int from_index, int to_index) {
        checkWritable();
        Objects.checkFromToIndex(from_index, to_index, size);
        if (to_index <= from_index) {
            return false;
        }
        if (to_index < size) {
            // The roles are swapped on purpose: 'to' is the source and
            // 'from' the destination of this copy
            System.arraycopy(array, to_index, array, from_index, size - to_index);
        }
        size -= to_index - from_index;
        return true;
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

    /**
     * Adds every value in {@code [from_value, to_value)}.
     * An empty or inverted range adds nothing.
     *
     * @return {@code true} if at least one value was inserted
     */
    public boolean addRange(int from_value, int to_value) {
        checkWritable();
        if (to_value <= from_value) {
            return false;
        }
        // each search result is the first index of a value >= the bound
        int from = indexOf(from_value);
        from = from >= 0 ? from : ~from;
        int to = indexOf(to_value);
        to = to >= 0 ? to : ~to;
        long missed = (long) to_value - from_value - (to - from);
        if (missed == 0) {
            return false;
        }
        // a range wider than any possible array cannot be merged
        if (missed > Integer.MAX_VALUE - size) {
            throw new OutOfMemoryError();
        }
        int added = (int) missed;
        ensureCapacity(size + added);
        if (to < size) {
            // The roles are swapped on purpose: 'to' is the source and
            // 'from' the destination of this copy
            System.arraycopy(array, to, array, to + added, size - to);
        }
        // merge the range with the present values, largest first
        int i = to;
        int value = to_value;
        for (int write = to + added; write > from; write--) {
            if (i > from && array[i - 1] == value - 1) {
                array[write - 1] = array[i - 1];
                i--;
            } else {
                array[write - 1] = value - 1;
            }
            value--;
        }
        size += added;
        return true;
    }

    /**
     * @return {@code true} if at least one value was inserted
     */
    public boolean addAll(IntSet other) {
        checkWritable();
        Objects.requireNonNull(other);
        int length = other.size;
        if (length == 0) {
            return false;
        }
        ensureCapacity(size + length);
        int[] other_array = other.array;
        boolean changed = false;
        for (int i = 0; i < length; i++) {
            changed |= add(other_array[i]);
        }
        return changed;
    }

    /**
     * @return {@code true} if at least one value was inserted
     */
    public boolean addAll(int[] other) {
        checkWritable();
        Objects.requireNonNull(other);
        int length = other.length;
        if (length == 0) {
            return false;
        }
        ensureCapacity(size + length);
        boolean changed = false;
        for (int v : other) {
            changed |= add(v);
        }
        return changed;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    private void checkNotEmpty() {
        if (size == 0) {
            throw new NoSuchElementException();
        }
    }

    public int at(int index) {
        Objects.checkIndex(index, size);
        return array[index];
    }

    public int first() {
        checkNotEmpty();
        return at(0);
    }

    public int last() {
        checkNotEmpty();
        return at(size - 1);
    }

    public int[] toArray() {
        return Arrays.copyOf(array, size);
    }

    public IntStream stream() {
        return Arrays.stream(array, 0, size);
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

    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<>() {
            int cursor;       // Index of next element to return
            int lastRet = -1; // Index of last element returned; -1 if no such

            public boolean hasNext() {
                return cursor < size;
            }

            public Integer next() {
                int i = cursor;
                if (i >= size)
                    throw new NoSuchElementException();
                cursor = i + 1;
                return array[lastRet = i];
            }

            public void remove() {
                checkWritable();
                int i = lastRet;
                if (i < 0)
                    throw new IllegalStateException();
                removeAt(i);
                cursor = i;
                lastRet = -1;
            }
        };
    }
}
