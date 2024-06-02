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

import java.util.Comparator;
import java.util.Objects;

public final class AnnotationItem implements Mutable {

    public static final Comparator<AnnotationItem> COMPARATOR = (a, b) -> {
        int out = TypeId.COMPARATOR.compare(a.annotation.getType(), b.annotation.getType());
        if (out != 0) {
            return out;
        }

        // a.type == b.type, but a != b
        throw new IllegalStateException(
                "can`t compare annotations of the same type: " + a + " " + b);
    };

    public enum AnnotationVisibility {
        BUILD(0x00),
        RUNTIME(0x01),
        SYSTEM(0x02);

        private final int value;

        AnnotationVisibility(int value) {
            this.value = value;
        }

        public static AnnotationVisibility of(int int_value) {
            for (AnnotationVisibility type : values()) {
                if (int_value == type.value) {
                    return type;
                }
            }
            throw new IllegalStateException("unknown annotation visibility: " + int_value);
        }
    }

    public static AnnotationItem FastNative() {
        return new AnnotationItem(AnnotationVisibility.BUILD, TypeId.of(
                "dalvik.annotation.optimization.FastNative"));
    }

    public static AnnotationItem CriticalNative() {
        return new AnnotationItem(AnnotationVisibility.BUILD, TypeId.of(
                "dalvik.annotation.optimization.CriticalNative"));
    }

    public static AnnotationItem AnnotationDefault(EncodedAnnotation annotation) {
        return new AnnotationItem(AnnotationVisibility.SYSTEM, TypeId.of(
                "dalvik.annotation.AnnotationDefault"),
                new AnnotationElement("value", EncodedValue.of(annotation)));
    }

    public static AnnotationItem EnclosingClass(TypeId clazz) {
        return new AnnotationItem(AnnotationVisibility.SYSTEM, TypeId.of(
                "dalvik.annotation.EnclosingClass"),
                new AnnotationElement("value", EncodedValue.of(clazz)));
    }

    public static AnnotationItem EnclosingMethod(MethodId method) {
        return new AnnotationItem(AnnotationVisibility.SYSTEM, TypeId.of(
                "dalvik.annotation.EnclosingMethod"),
                new AnnotationElement("value", EncodedValue.of(method)));
    }

    public static AnnotationItem InnerClass(String name, int access_flags) {
        return new AnnotationItem(AnnotationVisibility.SYSTEM, TypeId.of(
                "dalvik.annotation.InnerClass"),
                new AnnotationElement("name", EncodedValue.of(name)),
                new AnnotationElement("accessFlags", EncodedValue.of(access_flags)));
    }

    public static AnnotationItem MemberClasses(TypeId... classes) {
        return new AnnotationItem(AnnotationVisibility.SYSTEM, TypeId.of(
                "dalvik.annotation.MemberClasses"), new AnnotationElement(
                "value", EncodedValue.of(classes)));
    }

    public static AnnotationItem MethodParameters(String[] names, int[] access_flags) {
        return new AnnotationItem(AnnotationVisibility.SYSTEM, TypeId.of(
                "dalvik.annotation.MethodParameters"),
                new AnnotationElement("names", EncodedValue.of(names)),
                new AnnotationElement("accessFlags", EncodedValue.of(access_flags)));
    }

    public static AnnotationItem Signature(String... value) {
        return new AnnotationItem(AnnotationVisibility.SYSTEM, TypeId.of(
                "dalvik.annotation.Signature"), new AnnotationElement(
                "value", EncodedValue.of(value)));
    }

    public static AnnotationItem Throws(TypeId... exceptions) {
        return new AnnotationItem(AnnotationVisibility.SYSTEM, TypeId.of(
                "dalvik.annotation.Throws"), new AnnotationElement(
                "value", EncodedValue.of(exceptions)));
    }

    private AnnotationVisibility visibility;
    private EncodedAnnotation annotation;

    public AnnotationItem(AnnotationVisibility visibility, EncodedAnnotation annotation) {
        setVisibility(visibility);
        setAnnotation(annotation);
    }

    public AnnotationItem(AnnotationVisibility visibility, TypeId type, AnnotationElement... elements) {
        setVisibility(visibility);
        setAnnotation(new EncodedAnnotation(type, elements));
    }

    public void setVisibility(AnnotationVisibility visibility) {
        this.visibility = Objects.requireNonNull(visibility,
                "annotation visibility can`t be null");
    }

    public AnnotationVisibility getVisibility() {
        return visibility;
    }

    public void setAnnotation(EncodedAnnotation annotation) {
        this.annotation = Objects.requireNonNull(annotation,
                "annotation can`t be null").mutate();
    }

    public EncodedAnnotation getAnnotation() {
        return annotation;
    }

    public static AnnotationItem read(RandomInput in, ReadContext context) {
        return new AnnotationItem(AnnotationVisibility.of(in.readUnsignedByte()),
                EncodedAnnotation.read(in, context));
    }

    public void collectData(DataCollector data) {
        data.fill(annotation);
    }

    public void write(WriteContext context, RandomOutput out) {
        out.writeByte(visibility.value);
        annotation.write(context, out);
    }

    static void writeSection(WriteContextImpl context, FileMap map,
                             RandomOutput out, AnnotationItem[] annotations) {
        if (annotations.length != 0) {
            map.annotations_off = (int) out.position();
            map.annotations_size = annotations.length;
        }
        for (AnnotationItem tmp : annotations) {
            int start = (int) out.position();
            tmp.write(context, out);
            context.addAnnotation(tmp, start);
        }
    }

    @Override
    public String toString() {
        return "AnnotationItem{" + "visibility = " + visibility + "; " + annotation + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AnnotationItem) {
            AnnotationItem aiobj = (AnnotationItem) obj;
            return Objects.equals(visibility, aiobj.visibility)
                    && Objects.equals(annotation, aiobj.annotation);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(visibility, annotation);
    }

    @Override
    public AnnotationItem mutate() {
        return new AnnotationItem(visibility, annotation);
    }
}
