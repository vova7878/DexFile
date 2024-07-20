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

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;

public final class EncodedAnnotation implements Mutable {

    private static final Comparator<MutableList<AnnotationElement>> AE_LIST_COMPARATOR
            = MutableList.getComparator(AnnotationElement.COMPARATOR);

    public static final Comparator<EncodedAnnotation> COMPARATOR = (a, b) -> {
        int out = TypeId.COMPARATOR.compare(a.type, b.type);
        if (out != 0) {
            return out;
        }

        return AE_LIST_COMPARATOR.compare(a.elements, b.elements);
    };

    private TypeId type;

    private MutableList<AnnotationElement> elements;

    public EncodedAnnotation(TypeId type, Collection<AnnotationElement> elements) {
        setType(type);
        setElements(elements);
    }

    public EncodedAnnotation(TypeId type, AnnotationElement... elements) {
        this(type, new MutableList<>(elements));
    }

    public void setType(TypeId type) {
        this.type = Objects.requireNonNull(type,
                "type can`t be null").mutate();
    }

    public TypeId getType() {
        return type;
    }

    public void setElements(Collection<AnnotationElement> elements) {
        this.elements = elements == null
                ? MutableList.empty() : new MutableList<>(elements);
    }

    public MutableList<AnnotationElement> getElements() {
        return elements;
    }

    public static EncodedAnnotation read(RandomInput in, ReadContext context) {
        TypeId type = context.type(in.readULeb128());
        int size = in.readULeb128();
        MutableList<AnnotationElement> elements = MutableList.empty();
        for (int i = 0; i < size; i++) {
            elements.add(AnnotationElement.read(in, context));
        }
        return new EncodedAnnotation(type, elements);
    }

    public void collectData(DataCollector data) {
        data.add(type);
        for (AnnotationElement tmp : elements) {
            data.fill(tmp);
        }
    }

    public void write(WriteContext context, RandomOutput out) {
        out.writeULeb128(context.getTypeIndex(type));
        out.writeULeb128(elements.size());
        AnnotationElement[] sorted_elements = elements.toArray(new AnnotationElement[0]);
        Arrays.sort(sorted_elements, AnnotationElement.COMPARATOR);
        for (AnnotationElement tmp : sorted_elements) {
            tmp.write(context, out);
        }
    }

    @Override
    public String toString() {
        String elems = elements.stream()
                .map(AnnotationElement::toString)
                .collect(Collectors.joining(", "));
        return "EncodedAnnotation{" + type + " " + elems + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof EncodedAnnotation eaobj
                && Objects.equals(type, eaobj.type)
                && Objects.equals(elements, eaobj.elements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, elements);
    }

    @Override
    public EncodedAnnotation mutate() {
        return new EncodedAnnotation(type, elements);
    }
}
