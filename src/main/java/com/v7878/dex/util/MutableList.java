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
import com.v7878.misc.Checks;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;

public class MutableList<T extends Mutable>
        extends AbstractList<T> implements Mutable {

    public static <E extends Mutable, L extends MutableList<E>>
    Comparator<L> getComparator(Comparator<E> cmp) {
        Objects.requireNonNull(cmp);
        return (a, b) -> {
            int size = Math.min(a.size(), b.size());

            for (int i = 0; i < size; i++) {
                int out = cmp.compare(a.get(i), b.get(i));
                if (out != 0) {
                    return out;
                }
            }

            return Integer.compare(a.size(), b.size());
        };
    }

    private final ArrayList<T> elements;

    public MutableList(int initialCapacity) {
        this.elements = new ArrayList<>(initialCapacity);
    }

    @SafeVarargs
    public MutableList(T... elements) {
        int length = 0;
        if (elements != null) {
            length = elements.length;
        }
        this.elements = new ArrayList<>(length);
        if (length != 0) {
            addAll(elements);
        }
    }

    public MutableList(Collection<? extends T> c) {
        int length = c.size();
        this.elements = new ArrayList<>(length);
        if (length != 0) {
            addAll(c);
        }
    }

    public static <E extends Mutable> MutableList<E> empty() {
        return new MutableList<>();
    }

    protected T check(T element) {
        return Objects.requireNonNull(element,
                "MutableList can`t contain null element");
    }

    @Override
    @SuppressWarnings("unchecked")
    public final boolean add(T element) {
        return elements.add((T) check(element).mutate());
    }

    @Override
    @SuppressWarnings("unchecked")
    public final void add(int index, T element) {
        elements.add(index, (T) check(element).mutate());
    }

    @Override
    @SuppressWarnings("unchecked")
    public final T set(int index, T element) {
        return elements.set(index, (T) check(element).mutate());
    }

    @Override
    public final T get(int index) {
        return elements.get(index);
    }

    @Override
    public final T remove(int index) {
        return elements.remove(index);
    }

    @Override
    public final void clear() {
        elements.clear();
    }

    public void ensureCapacity(int minCapacity) {
        elements.ensureCapacity(minCapacity);
    }

    public void trimToSize() {
        elements.trimToSize();
    }

    public final boolean addAll(int index, T[] data, int from, int to) {
        ensureCapacity(size() + to - from);
        Checks.checkFromToIndex(from, to, data.length);
        if (to <= from) {
            return false;
        }
        for (int i = 0; i < to - from; i++) {
            add(index + i, data[from + i]);
        }
        return true;
    }

    public final boolean addAll(T[] data, int from, int to) {
        return addAll(size(), data, from, to);
    }

    public final boolean addAll(int index, T[] data) {
        return addAll(index, data, 0, data.length);
    }

    public final boolean addAll(T[] data) {
        return addAll(size(), data, 0, data.length);
    }

    @Override
    public final int size() {
        return elements.size();
    }

    @Override
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MutableList) {
            MutableList<?> tlobj = (MutableList<?>) obj;
            return Objects.equals(elements, tlobj.elements);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(elements);
    }

    @Override
    public MutableList<T> mutate() {
        return new MutableList<>(this);
    }
}
