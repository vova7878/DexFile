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
import com.v7878.dex.util.MutableList;

import java.util.Collection;

public final class AnnotationSetList extends MutableList<AnnotationSet> {

    public static final int ALIGNMENT = 4;

    public AnnotationSetList(int initialCapacity) {
        super(initialCapacity);
    }

    public AnnotationSetList(AnnotationSet... annotation_sets) {
        super(annotation_sets);
    }

    public AnnotationSetList(Collection<AnnotationSet> annotation_sets) {
        super(annotation_sets);
    }

    public static AnnotationSetList empty() {
        return new AnnotationSetList();
    }

    public static AnnotationSetList read(RandomInput in, ReadContext context) {
        AnnotationSetList out = new AnnotationSetList();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            int annotations_off = in.readInt();
            if (annotations_off != 0) {
                out.add(AnnotationSet.read(
                        in.duplicate(annotations_off), context));
            } else {
                out.add(AnnotationSet.empty());
            }
        }
        return out;
    }

    public void collectData(DataCollector data) {
        for (AnnotationSet tmp : this) {
            if (!tmp.isEmpty()) {
                data.add(tmp);
            }
        }
    }

    public void write(WriteContext context, RandomOutput out) {
        int max_size = size();
        while (get(max_size - 1).isEmpty()) {
            max_size--;
        }
        out.writeInt(max_size);
        for (int i = 0; i < max_size; i++) {
            AnnotationSet tmp = get(i);
            if (tmp.isEmpty()) {
                out.writeInt(0);
            } else {
                out.writeInt(context.getAnnotationSetOffset(tmp));
            }
        }
    }

    @Override
    public boolean isEmpty() {
        if (super.isEmpty()) {
            return true;
        }
        for (int i = 0; i < size(); i++) {
            if (!get(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected AnnotationSet check(AnnotationSet annotation_set) {
        return annotation_set == null ? AnnotationSet.empty() : annotation_set;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AnnotationSetList && super.equals(obj);
    }

    // hashcode from super

    @Override
    public AnnotationSetList mutate() {
        return new AnnotationSetList(this);
    }
}
