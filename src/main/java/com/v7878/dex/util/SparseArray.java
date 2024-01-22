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

    public void removeAt(int index) {
        if (index >= size) {
            // The array might be slightly bigger than mSize, in which case, indexing won't fail.
            throw new ArrayIndexOutOfBoundsException(index);
        }
        if (values[index] != DELETED) {
            values[index] = DELETED;
            garbage = true;
        }
    }

    public void removeAtRange(int index, int length) {
        final int end = Math.min(size, index + length);
        for (int i = index; i < end; i++) {
            removeAt(i);
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

    private static <T> T[] insert(T[] arr, int size, int index, T value) {
        T[] out = Arrays.copyOf(arr, unpadded_length(size + 1));
        out[index] = value;
        if (index < size) {
            System.arraycopy(arr, index, out, index + 1, size - index);
        }
        return out;
    }

    private static int[] insert(int[] arr, int size, int index, int value) {
        int[] out = Arrays.copyOf(arr, unpadded_length(size + 1));
        out[index] = value;
        if (index < size) {
            System.arraycopy(arr, index, out, index + 1, size - index);
        }
        return out;
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

    public int size() {
        if (garbage) {
            gc();
        }
        return size;
    }

    public int keyAt(int index) {
        if (garbage) {
            gc();
        }
        if (index >= size) {
            // The array might be slightly bigger than mSize, in which case, indexing won't fail.
            throw new ArrayIndexOutOfBoundsException(index);
        }
        return keys[index];
    }

    @SuppressWarnings("unchecked")
    public E valueAt(int index) {
        if (garbage) {
            gc();
        }
        if (index >= size) {
            // The array might be slightly bigger than mSize, in which case, indexing won't fail.
            throw new ArrayIndexOutOfBoundsException(index);
        }
        return (E) values[index];
    }

    public void setValueAt(int index, E value) {
        if (garbage) {
            gc();
        }
        if (index >= size) {
            // The array might be slightly bigger than mSize, in which case, indexing won't fail.
            throw new ArrayIndexOutOfBoundsException(index);
        }
        values[index] = value;
    }

    public int indexOfKey(int key) {
        if (garbage) {
            gc();
        }
        return Arrays.binarySearch(keys, 0, size, key);
    }

    public int indexOfValue(E value) {
        if (garbage) {
            gc();
        }
        for (int i = 0; i < size; i++) {
            if (values[i] == value) {
                return i;
            }
        }
        return -1;
    }

    public int indexOfValueByValue(E value) {
        if (garbage) {
            gc();
        }
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
        if (size() <= 0) {
            return "{}";
        }
        StringBuilder buffer = new StringBuilder(size * 28);
        buffer.append('{');
        for (int i = 0; i < size; i++) {
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
        int size = size();
        if (size != other.size()) {
            return false;
        }
        // size() calls above took care about gc() compaction.
        for (int index = 0; index < size; index++) {
            if (keys[index] != other.keys[index]
                    || !Objects.equals(values[index], other.values[index])) {
                return false;
            }
        }
        return true;
    }

    public int contentHashCode() {
        int hash = 0;
        int size = size();
        // size() call above took care about gc() compaction.
        for (int index = 0; index < size; index++) {
            int key = keys[index];
            Object value = values[index];
            hash = 31 * hash + key;
            hash = 31 * hash + Objects.hashCode(value);
        }
        return hash;
    }
}
