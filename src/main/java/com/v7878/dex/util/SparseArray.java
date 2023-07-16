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

public class SparseArray<E> {
    private int[] keys;
    private Object[] values;
    private int size;

    public SparseArray() {
        this(5);
    }

    private static int unpadded_length(int min_length) {
        return (min_length & 1) == 0 ? min_length + 1 : min_length;
    }

    public SparseArray(int initialCapacity) {
        values = new Object[unpadded_length(initialCapacity)];
        keys = new int[values.length];
        size = 0;
    }

    public E get(int key) {
        return get(key, null);
    }

    @SuppressWarnings("unchecked")
    public E get(int key, E valueIfKeyNotFound) {
        int i = Arrays.binarySearch(keys, 0, size, key);
        return i < 0 ? valueIfKeyNotFound : (E) values[i];
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
            keys = insert(keys, size, i, key);
            values = insert(values, size, i, value);
            size++;
        }
    }
}
