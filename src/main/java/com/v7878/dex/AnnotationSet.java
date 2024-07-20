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

package com.v7878.dex;

import com.v7878.dex.io.RandomInput;
import com.v7878.dex.io.RandomOutput;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

//TODO: maybe make MutableSet?
public final class AnnotationSet extends AbstractSet<AnnotationItem> implements Mutable {

    public static final int ALIGNMENT = 4;

    private final Set<AnnotationItem> annotations;

    public AnnotationSet(int initialCapacity) {
        this.annotations = new HashSet<>(initialCapacity);
    }

    public AnnotationSet(AnnotationItem... annotations) {
        int length = 0;
        if (annotations != null) {
            length = annotations.length;
        }
        this.annotations = new HashSet<>(length);
        if (length != 0) {
            addAll(Arrays.asList(annotations));
        }
    }

    public AnnotationSet(Collection<AnnotationItem> annotations) {
        int length = annotations.size();
        this.annotations = new HashSet<>(length);
        if (length != 0) {
            addAll(annotations);
        }
    }

    public static AnnotationSet empty() {
        return new AnnotationSet();
    }

    public static AnnotationSet read(RandomInput in, ReadContext context) {
        int size = in.readInt();
        AnnotationItem[] out = new AnnotationItem[size];
        for (int i = 0; i < size; i++) {
            out[i] = AnnotationItem.read(context.data(in.readInt()), context);
        }
        return new AnnotationSet(out);
    }

    public void collectData(DataCollector data) {
        for (AnnotationItem tmp : annotations) {
            data.add(tmp);
        }
    }

    public void write(WriteContext context, RandomOutput out) {
        AnnotationItem[] sorted_annotations = annotations.toArray(new AnnotationItem[0]);
        Arrays.sort(sorted_annotations, AnnotationItem.COMPARATOR);
        out.writeInt(size());
        for (AnnotationItem tmp : sorted_annotations) {
            out.writeInt(context.getAnnotationOffset(tmp));
        }
    }

    static void writeSection(WriteContextImpl context, FileMap map,
                             RandomOutput out, AnnotationSet[] annotation_sets) {
        if (annotation_sets.length != 0) {
            out.alignPosition(ALIGNMENT);
            map.annotation_sets_off = (int) out.position();
            map.annotation_sets_size = annotation_sets.length;
        }
        for (AnnotationSet tmp : annotation_sets) {
            int start = (int) out.position();
            tmp.write(context, out);
            context.addAnnotationSet(tmp, start);
        }
    }

    @Override
    public void clear() {
        annotations.clear();
    }

    @Override
    public boolean isEmpty() {
        return annotations.isEmpty();
    }

    @Override
    public boolean contains(Object obj) {
        return annotations.contains(obj);
    }

    private AnnotationItem check(AnnotationItem annotation) {
        return Objects.requireNonNull(annotation,
                "annotation set can`t contain null annotation");
    }

    @Override
    public boolean add(AnnotationItem annotation) {
        return annotations.add(check(annotation).mutate());
    }

    @Override
    public boolean remove(Object obj) {
        return annotations.remove(obj);
    }

    @Override
    public Iterator<AnnotationItem> iterator() {
        return annotations.iterator();
    }

    @Override
    public int size() {
        return annotations.size();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof AnnotationSet asobj
                && Objects.equals(annotations, asobj.annotations);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(annotations);
    }

    @Override
    public AnnotationSet mutate() {
        return new AnnotationSet(this);
    }
}
