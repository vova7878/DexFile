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
