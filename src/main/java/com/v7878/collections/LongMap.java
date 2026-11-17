package com.v7878.collections;

import com.v7878.dex.util.EmptyArrays;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.LongFunction;

public final class LongMap<E> implements Iterable<LongMap.Entry<E>> {
    private static final Object DELETED = new Object();

    private boolean garbage;
    private boolean ro;
    private long[] keys;
    private Object[] values;
    private int size;

    public LongMap() {
        this(0);
    }

    public LongMap(int capacity) {
        this.keys = capacity <= 0 ? EmptyArrays.LONG : new long[capacity];
        this.values = capacity <= 0 ? EmptyArrays.OBJECT : new Object[capacity];
        this.size = 0;
        this.garbage = false;
        this.ro = false;
    }

    public LongMap(LongMap<E> other) {
        var length = other.size;
        this.size = length;
        if (length == 0) {
            this.keys = EmptyArrays.LONG;
            this.values = EmptyArrays.OBJECT;
            this.garbage = false;
        } else {
            this.keys = other.keys.clone();
            this.values = other.values.clone();
            this.garbage = other.garbage;
        }
        this.ro = false;
    }

    LongMap(LongMap<E> other, boolean ro) {
        var length = other.size;
        this.size = length;
        if (length == 0) {
            this.keys = EmptyArrays.LONG;
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

    LongMap(long[] keys, Object[] values, int size, boolean garbage, boolean ro) {
        this.keys = keys;
        this.values = values;
        this.size = size;
        this.garbage = garbage;
        this.ro = ro;
    }

    private static final LongMap<Object> EMPTY = new LongMap<>(
            EmptyArrays.LONG, EmptyArrays.OBJECT, 0, false, true);

    @SuppressWarnings("unchecked")
    public static <T> LongMap<T> empty() {
        return (LongMap<T>) EMPTY;
    }

    /**
     * A frozen map returns itself; a mutable map returns a writable copy.
     */
    public LongMap<E> duplicate() {
        return ro ? this : new LongMap<>(this, false);
    }

    public LongMap<E> freeze() {
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

    private int indexOfKeyRaw(long key) {
        return ArraySupport.binarySearch(keys, 0, size, key);
    }

    public int indexOfKey(long key) {
        gcIfNeeded();
        return indexOfKeyRaw(key);
    }

    public boolean contains(long key) {
        int i = indexOfKeyRaw(key);
        return i >= 0 && values[i] != DELETED;
    }

    public E get(long key) {
        return get(key, null);
    }

    @SuppressWarnings("unchecked")
    public E get(long key, E valueIfKeyNotFound) {
        int i = indexOfKeyRaw(key);
        return i < 0 || values[i] == DELETED ? valueIfKeyNotFound : (E) values[i];
    }

    @SuppressWarnings("unchecked")
    public E remove(long key) {
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

    /**
     * @return the previous value at {@code index}; {@code null} when
     * the removed value was {@code null}
     */
    @SuppressWarnings("unchecked")
    public E removeAt(int index) {
        checkWritable();
        Objects.checkIndex(index, size());
        // size() call above took care about gc() compaction
        Object last = values[index];
        values[index] = DELETED;
        garbage = true;
        return (E) last;
    }

    /**
     * Removes every entry with a key in {@code [from_key, to_key)}.
     * An empty or inverted range removes nothing.
     *
     * @return {@code true} if at least one entry was removed
     */
    public boolean removeRange(long from_key, long to_key) {
        checkWritable();
        if (to_key <= from_key) {
            return false;
        }
        // each search result is the first index of a key >= the bound
        int from = indexOfKeyRaw(from_key);
        from = from >= 0 ? from : ~from;
        int to = indexOfKeyRaw(to_key);
        to = to >= 0 ? to : ~to;
        boolean changed = false;
        for (int i = from; i < to; i++) {
            if (values[i] != DELETED) {
                values[i] = DELETED;
                changed = true;
            }
        }
        if (changed) {
            garbage = true;
        }
        return changed;
    }

    /**
     * @return {@code true} if at least one entry was removed
     */
    public boolean removeAtRange(int from_index, int to_index) {
        checkWritable();
        Objects.checkFromToIndex(from_index, to_index, size());
        if (to_index <= from_index) {
            return false;
        }
        // size() call above took care about gc() compaction
        Arrays.fill(values, from_index, to_index, DELETED);
        garbage = true;
        return true;
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

    /**
     * @return the previous value associated with {@code key}, or
     * {@code null} if there was no mapping for {@code key}.
     * (A {@code null} return can also indicate that the map
     * previously associated {@code null} with {@code key})
     */
    @SuppressWarnings("unchecked")
    public E put(long key, E value) {
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
     * Puts a key/value pair into the map, optimizing for the case
     * where the key is greater than all existing keys in the array
     */
    public E append(long key, E value) {
        checkWritable();
        if (size != 0 && key <= keys[size - 1]) {
            return put(key, value);
        }
        if (garbage && size >= keys.length) {
            gc();
        }
        keys = ArraySupport.insert(keys, size, size, key);
        values = ArraySupport.insert(values, size, size, value);
        size++;
        return null;
    }

    @SuppressWarnings("unchecked")
    public void putAll(LongMap<? extends E> other) {
        checkWritable();
        int length = other.size;
        if (length == 0) {
            return;
        }
        ensureCapacity(size + length);
        long[] other_keys = other.keys;
        Object[] other_values = other.values;
        for (int i = 0; i < length; i++) {
            Object value = other_values[i];
            if (value != DELETED) {
                put(other_keys[i], (E) value);
            }
        }
    }

    /**
     * Replaces the entry for the specified key only if it is
     * currently mapped to some value.
     *
     * @return the previous value associated with the specified key, or
     * {@code null} if there was no mapping for the key.
     * (A {@code null} return can also indicate that the map
     * previously associated {@code null} with the key)
     */
    @SuppressWarnings("unchecked")
    public E replace(long key, E value) {
        checkWritable();
        int i = indexOfKeyRaw(key);
        if (i >= 0) {
            Object last = values[i];
            if (last != DELETED) {
                values[i] = value;
                return (E) last;
            }
            return null;
        }
        return null;
    }

    /**
     * If the specified key is not already associated with a value associates
     * it with the given value and returns {@code null}, else returns the current value.
     *
     * @return the previous value associated with the specified key, or
     * {@code null} if there was no mapping for the key.
     * (A {@code null} return can also indicate that the map
     * previously associated {@code null} with the key)
     */
    @SuppressWarnings("unchecked")
    public E putIfAbsent(long key, E value) {
        checkWritable();
        int i = indexOfKeyRaw(key);
        if (i >= 0) {
            Object last = values[i];
            if (last == DELETED) {
                values[i] = value;
                return null;
            }
            return (E) last;
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
     * If the specified key is not already associated with a value,
     * attempts to compute its value using the given mapping
     * function and enters it into this map.
     *
     * @return the current (existing or computed) value associated with
     * the specified key, or null if the computed value is null
     */
    @SuppressWarnings("unchecked")
    public E computeIfAbsent(long key, LongFunction<? extends E> mapper) {
        checkWritable();
        int i = indexOfKeyRaw(key);
        if (i >= 0) {
            Object last = values[i];
            if (last == DELETED) {
                E new_value = mapper.apply(key);
                values[i] = new_value;
                return new_value;
            }
            return (E) last;
        }
        i = ~i;
        if (i < size && values[i] == DELETED) {
            E new_value = mapper.apply(key);
            keys[i] = key;
            values[i] = new_value;
            return new_value;
        }
        if (garbage && size >= keys.length) {
            gc();
            // Search again because indices may have changed
            i = ~indexOfKeyRaw(key);
        }
        E new_value = mapper.apply(key);
        keys = ArraySupport.insert(keys, size, i, key);
        values = ArraySupport.insert(values, size, i, new_value);
        size++;
        return new_value;
    }

    public int size() {
        gcIfNeeded();
        return size;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    private void checkNotEmpty() {
        if (size() == 0) {
            throw new NoSuchElementException();
        }
    }

    public long keyAt(int index) {
        Objects.checkIndex(index, size());
        // size() call above took care about gc() compaction
        return keys[index];
    }

    public long firstKey() {
        checkNotEmpty();
        return keyAt(0);
    }

    public long lastKey() {
        checkNotEmpty();
        return keyAt(size() - 1);
    }

    public long[] keysArray() {
        int size = size();
        // size() call above took care about gc() compaction
        return Arrays.copyOf(keys, size);
    }

    public LongSet keysSet() {
        var array = keysArray();
        return new LongSet(array, array.length, false);
    }

    @SuppressWarnings("unchecked")
    public E valueAt(int index) {
        Objects.checkIndex(index, size());
        // size() call above took care about gc() compaction
        return (E) values[index];
    }

    public E firstValue() {
        checkNotEmpty();
        return valueAt(0);
    }

    public E lastValue() {
        checkNotEmpty();
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

    public record Entry<T>(long key, T value) {
    }

    @SuppressWarnings("unchecked")
    public Entry<E> entryAt(int index) {
        Objects.checkIndex(index, size());
        // size() call above took care about gc() compaction
        return new Entry<>(keys[index], (E) values[index]);
    }

    public Entry<E> firstEntry() {
        checkNotEmpty();
        return entryAt(0);
    }

    public Entry<E> lastEntry() {
        checkNotEmpty();
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
            long key = keys[i];
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

    public boolean contentEquals(LongMap<?> other) {
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
        return obj instanceof LongMap<?> other
                && contentEquals(other);
    }

    public int contentHashCode() {
        int hash = 0;
        int length = size();
        // size() call above took care about gc() compaction
        for (int index = 0; index < length; index++) {
            int key = Long.hashCode(keys[index]);
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

    @Override
    public Iterator<Entry<E>> iterator() {
        return new Iterator<>() {
            int cursor;       // Index of next element to return
            int lastRet = -1; // Index of last element returned; -1 if no such

            public boolean hasNext() {
                return cursor < size();
            }

            @SuppressWarnings("unchecked")
            public Entry<E> next() {
                int i = cursor;
                if (i >= size())
                    throw new NoSuchElementException();
                cursor = i + 1;
                lastRet = i;
                // size() calls above took care about gc() compaction
                return new Entry<>(keys[i], (E) values[i]);
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
