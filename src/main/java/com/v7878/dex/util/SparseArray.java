/*
 * Copyright (c) 2023 Vladimir Kozelkov
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.v7878.dex.util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Objects;

public class SparseArray<E> implements Cloneable {
    private static final Object DELETED = new Object();

    private static int unpadded_length(int min_length) {
        return (min_length & 1) == 0 ? min_length + 1 : min_length;
    }

    private boolean garbage = false;
    private int[] keys;
    private Object[] values;
    private int size;

    public SparseArray() {
        this(0);
    }

    public SparseArray(int initialCapacity) {
        values = new Object[unpadded_length(initialCapacity)];
        keys = new int[values.length];
        size = 0;
    }

    @SuppressWarnings("CopyConstructorMissesField")
    public SparseArray(SparseArray<E> sparseArray) {
        this(sparseArray.size());
        putAll(sparseArray);
    }

    public void ensureCapacity(int minCapacity) {
        int new_length = unpadded_length(minCapacity);
        if (new_length > keys.length) {
            keys = Arrays.copyOf(keys, new_length);
            values = Arrays.copyOf(values, new_length);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public SparseArray<E> clone() {
        SparseArray<E> clone = null;
        try {
            clone = (SparseArray<E>) super.clone();
            clone.keys = keys.clone();
            clone.values = values.clone();
        } catch (CloneNotSupportedException cnse) {
            /* ignore */
        }
        return clone;
    }

    private void checkIndex(int index) {
        if (index >= size) {
            // The array might be slightly bigger than size, in which case, indexing won't fail.
            throw new ArrayIndexOutOfBoundsException(index);
        }
    }

    public boolean contains(int key) {
        return indexOfKey(key) >= 0;
    }

    public E get(int key) {
        return get(key, null);
    }

    @SuppressWarnings("unchecked")
    public E get(int key, E valueIfKeyNotFound) {
        int i = Arrays.binarySearch(keys, 0, size, key);
        return i < 0 || values[i] == DELETED ? valueIfKeyNotFound : (E) values[i];
    }

    public void remove(int key) {
        int i = Arrays.binarySearch(keys, 0, size, key);
        if (i >= 0) {
            if (values[i] != DELETED) {
                values[i] = DELETED;
                garbage = true;
            }
        }
    }

    private void removeAtNoGC(int index) {
        checkIndex(index);
        if (values[index] != DELETED) {
            values[index] = DELETED;
            garbage = true;
        }
    }

    public void removeAt(int index) {
        gcIfNeeded();
        removeAtNoGC(index);
    }

    public void removeAtRange(int index, int length) {
        gcIfNeeded();
        for (int i = 0; i < length; i++) {
            removeAtNoGC(index + length);
        }
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

    private static int growSize(int currentSize) {
        return currentSize <= 4 ? 8 : currentSize + currentSize / 2;
    }

    private static <T> T[] insert(T[] array, int currentSize, int index, T value) {
        if (currentSize + 1 <= array.length) {
            System.arraycopy(array, index, array, index + 1, currentSize - index);
            array[index] = value;
            return array;
        }
        @SuppressWarnings("unchecked")
        T[] newArray = (T[]) Array.newInstance(array.getClass().componentType(),
                unpadded_length(growSize(currentSize)));
        System.arraycopy(array, 0, newArray, 0, index);
        newArray[index] = value;
        System.arraycopy(array, index, newArray, index + 1, array.length - index);
        return newArray;
    }

    private static int[] insert(int[] array, int currentSize, int index, int value) {
        if (currentSize + 1 <= array.length) {
            System.arraycopy(array, index, array, index + 1, currentSize - index);
            array[index] = value;
            return array;
        }
        int[] newArray = new int[unpadded_length(growSize(currentSize))];
        System.arraycopy(array, 0, newArray, 0, index);
        newArray[index] = value;
        System.arraycopy(array, index, newArray, index + 1, array.length - index);
        return newArray;
    }

    public void put(int key, E value) {
        int i = Arrays.binarySearch(keys, 0, size, key);
        if (i >= 0) {
            values[i] = value;
        } else {
            i = ~i;
            if (i < size && values[i] == DELETED) {
                keys[i] = key;
                values[i] = value;
                return;
            }
            if (garbage && size >= keys.length) {
                gc();
                // Search again because indices may have changed.
                i = ~Arrays.binarySearch(keys, 0, size, key);
            }
            keys = insert(keys, size, i, key);
            values = insert(values, size, i, value);
            size++;
        }
    }

    /**
     * Puts a key/value pair into the array, optimizing for the case where
     * the key is greater than all existing keys in the array.
     */
    public void append(int key, E value) {
        //TODO: optimize
        put(key, value);
    }

    public void putAll(SparseArray<? extends E> other) {
        Objects.requireNonNull(other);
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
                //noinspection unchecked
                append(other_keys[i], (E) value);
            }
        }
    }

    public int size() {
        gcIfNeeded();
        return size;
    }

    public int keyAt(int index) {
        gcIfNeeded();
        checkIndex(index);
        return keys[index];
    }

    @SuppressWarnings("unchecked")
    public E valueAt(int index) {
        gcIfNeeded();
        checkIndex(index);
        return (E) values[index];
    }

    public void setValueAt(int index, E value) {
        gcIfNeeded();
        checkIndex(index);
        values[index] = value;
    }

    public int indexOfKey(int key) {
        gcIfNeeded();
        return Arrays.binarySearch(keys, 0, size, key);
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
        int count = size;
        for (int i = 0; i < count; i++) {
            values[i] = null;
        }
        size = 0;
        garbage = false;
    }

    @Override
    public String toString() {
        int length = size();
        if (length <= 0) {
            return "{}";
        }
        // size() calls above took care about gc() compaction.
        StringBuilder buffer = new StringBuilder(length * 28);
        buffer.append('{');
        for (int i = 0; i < length; i++) {
            if (i > 0) {
                buffer.append(", ");
            }
            int key = keyAt(i);
            buffer.append(key);
            buffer.append('=');
            Object value = valueAt(i);
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
        // size() calls above took care about gc() compaction.
        for (int index = 0; index < length; index++) {
            if (keys[index] != other.keys[index] ||
                    !Objects.equals(values[index], other.values[index])) {
                return false;
            }
        }
        return true;
    }

    public int contentHashCode() {
        int hash = 0;
        int length = size();
        // size() call above took care about gc() compaction.
        for (int index = 0; index < length; index++) {
            int key = keys[index];
            Object value = values[index];
            hash = 31 * hash + key;
            hash = 31 * hash + Objects.hashCode(value);
        }
        return hash;
    }

    public void trimToSize() {
        int length = unpadded_length(size());
        // size() call above took care about gc() compaction.
        keys = Arrays.copyOf(keys, length);
        values = Arrays.copyOf(values, length);
    }
}
