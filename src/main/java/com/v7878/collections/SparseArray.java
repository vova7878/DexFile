package com.v7878.collections;

import com.v7878.dex.util.EmptyArrays;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntFunction;

public final class SparseArray<E> {
    private static final Object DELETED = new Object();

    private boolean garbage;
    private boolean ro;
    private int[] keys;
    private Object[] values;
    private int size;

    public SparseArray() {
        this(0);
    }

    public SparseArray(int capacity) {
        this.keys = capacity <= 0 ? EmptyArrays.INT : new int[capacity];
        this.values = capacity <= 0 ? EmptyArrays.OBJECT : new Object[capacity];
        this.size = 0;
        this.garbage = false;
        this.ro = false;
    }

    public SparseArray(SparseArray<E> other) {
        var length = other.size;
        this.size = length;
        if (length == 0) {
            this.keys = EmptyArrays.INT;
            this.values = EmptyArrays.OBJECT;
            this.garbage = false;
        } else {
            this.keys = other.keys.clone();
            this.values = other.values.clone();
            this.garbage = other.garbage;
        }
        this.ro = false;
    }

    SparseArray(SparseArray<E> other, boolean ro) {
        var length = other.size;
        this.size = length;
        if (length == 0) {
            this.keys = EmptyArrays.INT;
            this.values = EmptyArrays.OBJECT;
            this.garbage = false;
        } else {
            this.garbage = other.garbage;
            if (ro && this.garbage) {
                this.keys = other.keys;
                this.values = other.values;
                var trimmed = trimToSize();
                assert trimmed;
            } else {
                this.keys = other.keys.clone();
                this.values = other.values.clone();
            }
        }
        this.ro = ro;
    }

    SparseArray(int[] keys, Object[] values, int size, boolean garbage, boolean ro) {
        this.keys = keys;
        this.values = values;
        this.size = size;
        this.garbage = garbage;
        this.ro = ro;
    }

    private static final SparseArray<Object> EMPTY = new SparseArray<>(
            EmptyArrays.INT, EmptyArrays.OBJECT, 0, false, true);

    @SuppressWarnings("unchecked")
    public static <T> SparseArray<T> empty() {
        return (SparseArray<T>) EMPTY;
    }

    public SparseArray<E> duplicate() {
        return new SparseArray<>(this, ro);
    }

    public SparseArray<E> freeze() {
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

    public void ensureCapacity(int min_capacity) {
        checkWritable();
        if (min_capacity > keys.length) {
            keys = Arrays.copyOf(keys, min_capacity);
            values = Arrays.copyOf(values, min_capacity);
        }
    }

    private int indexOfKeyRaw(int key) {
        gcIfNeeded();
        return ArraySupport.binarySearch(keys, 0, size, key);
    }

    public int indexOfKey(int key) {
        gcIfNeeded();
        return indexOfKeyRaw(key);
    }

    public boolean contains(int key) {
        return indexOfKey(key) >= 0;
    }

    public E get(int key) {
        return get(key, null);
    }

    @SuppressWarnings("unchecked")
    public E get(int key, E valueIfKeyNotFound) {
        int i = indexOfKeyRaw(key);
        return i < 0 || values[i] == DELETED ? valueIfKeyNotFound : (E) values[i];
    }

    @SuppressWarnings("unchecked")
    public E remove(int key) {
        checkWritable();
        int i = indexOfKeyRaw(key);
        if (i >= 0) {
            Object last = values[i];
            if (last != DELETED) {
                values[i] = DELETED;
                garbage = true;
                return (E) last;
            }
        }
        return null;
    }

    public void removeAt(int index) {
        checkWritable();
        Objects.checkIndex(index, size());
        // size() call above took care about gc() compaction
        if (values[index] != DELETED) {
            values[index] = DELETED;
            garbage = true;
        }
    }

    public void removeAtRange(int index, int length) {
        checkWritable();
        Objects.checkFromIndexSize(index, length, size());
        if (length <= 0) {
            return;
        }
        // size() call above took care about gc() compaction
        Arrays.fill(values, index, index + length, DELETED);
        garbage = true;
    }

    private void gc() {
        int count = size;
        int new_size = 0;
        for (int i = 0; i < count; i++) {
            Object tmp = values[i];
            if (tmp != DELETED) {
                if (i != new_size) {
                    keys[new_size] = keys[i];
                    values[new_size] = tmp;
                    values[i] = null;
                }
                new_size++;
            }
        }
        size = new_size;
        garbage = false;
    }

    private void gcIfNeeded() {
        if (garbage) {
            gc();
        }
    }

    @SuppressWarnings("unchecked")
    public E put(int key, E value) {
        checkWritable();
        int i = indexOfKeyRaw(key);
        if (i >= 0) {
            Object last = values[i];
            values[i] = value;
            return last == DELETED ? null : (E) last;
        }
        i = ~i;
        if (i < size && values[i] == DELETED) {
            keys[i] = key;
            values[i] = value;
            return null;
        }
        if (garbage && size >= keys.length) {
            gc();
            // Search again because indices may have changed
            i = ~indexOfKeyRaw(key);
        }
        keys = ArraySupport.insert(keys, size, i, key);
        values = ArraySupport.insert(values, size, i, value);
        size++;
        return null;
    }

    /**
     * Puts a key/value pair into the array, optimizing for the case
     * where the key is greater than all existing keys in the array
     */
    public E append(int key, E value) {
        checkWritable();
        //TODO: optimize
        return put(key, value);
    }

    @SuppressWarnings("unchecked")
    public void putAll(SparseArray<? extends E> other) {
        checkWritable();
        int length = other.size;
        if (length == 0) {
            return;
        }
        ensureCapacity(size + length);
        int[] other_keys = other.keys;
        Object[] other_values = other.values;
        for (int i = 0; i < length; i++) {
            Object value = other_values[i];
            if (value != DELETED) {
                put(other_keys[i], (E) value);
            }
        }
    }

    public int size() {
        gcIfNeeded();
        return size;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public int keyAt(int index) {
        Objects.checkIndex(index, size());
        // size() call above took care about gc() compaction
        return keys[index];
    }

    public int firstKey() {
        return keyAt(0);
    }

    public int lastKey() {
        return keyAt(size() - 1);
    }

    public int[] keysArray() {
        int size = size();
        // size() call above took care about gc() compaction
        return Arrays.copyOf(keys, size);
    }

    public IntSet keysSet() {
        var array = keysArray();
        return new IntSet(array, array.length, false);
    }

    @SuppressWarnings("unchecked")
    public E valueAt(int index) {
        Objects.checkIndex(index, size());
        // size() call above took care about gc() compaction
        return (E) values[index];
    }

    public E firstValue() {
        return valueAt(0);
    }

    public E lastValue() {
        return valueAt(size() - 1);
    }

    @SuppressWarnings("unchecked")
    public E setValueAt(int index, E value) {
        checkWritable();
        Objects.checkIndex(index, size());
        // size() call above took care about gc() compaction
        Object last = values[index];
        values[index] = value;
        return last == DELETED ? null : (E) last;
    }

    @SuppressWarnings("SuspiciousSystemArraycopy")
    public <T> T[] valuesArray(IntFunction<T[]> factory) {
        int length = size();
        T[] array = factory.apply(length);
        // size() calls above took care about gc() compaction
        System.arraycopy(values, 0, array, 0, length);
        return array;
    }

    public Object[] valuesArray() {
        return valuesArray(Object[]::new);
    }

    public record Entry<T>(int key, T value) {
    }

    @SuppressWarnings("unchecked")
    public Entry<E> entryAt(int index) {
        Objects.checkIndex(index, size());
        // size() call above took care about gc() compaction
        return new Entry<>(keys[index], (E) values[index]);
    }

    public Entry<E> firstEntry() {
        return entryAt(0);
    }

    public Entry<E> lastEntry() {
        return entryAt(size() - 1);
    }

    public int indexOfValue(E value) {
        gcIfNeeded();
        for (int i = 0; i < size; i++) {
            if (values[i] == value) {
                return i;
            }
        }
        return -1;
    }

    public int indexOfValueByValue(E value) {
        gcIfNeeded();
        for (int i = 0; i < size; i++) {
            if (Objects.equals(values[i], value)) {
                return i;
            }
        }
        return -1;
    }

    public void clear() {
        checkWritable();
        Arrays.fill(values, 0, size, null);
        size = 0;
        garbage = false;
    }

    @Override
    public String toString() {
        int length = size();
        if (length <= 0) {
            return "{}";
        }
        // size() calls above took care about gc() compaction
        StringBuilder buffer = new StringBuilder(length * 28);
        buffer.append('{');
        for (int i = 0; i < length; i++) {
            if (i > 0) {
                buffer.append(", ");
            }
            int key = keys[i];
            buffer.append(key);
            buffer.append('=');
            Object value = values[i];
            if (value != this) {
                buffer.append(value);
            } else {
                buffer.append("(this)");
            }
        }
        buffer.append('}');
        return buffer.toString();
    }

    public boolean contentEquals(SparseArray<?> other) {
        if (other == null) {
            return false;
        }
        int length = size();
        if (length != other.size()) {
            return false;
        }
        // size() calls above took care about gc() compaction
        for (int index = 0; index < length; index++) {
            if (keys[index] != other.keys[index] ||
                    !Objects.equals(values[index], other.values[index])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return obj instanceof SparseArray<?> other
                && contentEquals(other);
    }

    public int contentHashCode() {
        int hash = 0;
        int length = size();
        // size() call above took care about gc() compaction
        for (int index = 0; index < length; index++) {
            int key = keys[index];
            Object value = values[index];
            hash = 31 * hash + key;
            hash = 31 * hash + Objects.hashCode(value);
        }
        return hash;
    }

    @Override
    public int hashCode() {
        return contentHashCode();
    }

    public boolean trimToSize() {
        checkWritable();
        int length = size();
        // size() call above took care about gc() compaction
        if (keys.length > length) {
            keys = Arrays.copyOf(keys, length);
            values = Arrays.copyOf(values, length);
            return true;
        }
        return false;
    }
}
