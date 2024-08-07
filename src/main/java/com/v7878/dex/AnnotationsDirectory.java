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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

// Temporary object. Needed to read or write
//TODO: clean up code
final class AnnotationsDirectory {

    public static final int ALIGNMENT = 4;

    public AnnotationSet class_annotations;
    public Map<FieldId, AnnotationSet> annotated_fields;
    public Map<MethodId, AnnotationSet> annotated_methods;
    public Map<MethodId, AnnotationSetList> annotated_parameters;

    public void setClassAnnotations(AnnotationSet class_annotations) {
        this.class_annotations = class_annotations;
    }

    public void addFieldAnnotations(FieldId field, AnnotationSet annotations) {
        if (annotations.isEmpty()) {
            throw new IllegalStateException("field annotations is empty");
        }
        if (annotated_fields.putIfAbsent(field, annotations) != null) {
            throw new IllegalStateException("annotated_fields contain duplicates");
        }
    }

    public void addMethodAnnotations(MethodId method, AnnotationSet annotations) {
        if (annotations.isEmpty()) {
            throw new IllegalStateException("method annotations is empty");
        }
        if (annotated_methods.putIfAbsent(method, annotations) != null) {
            throw new IllegalStateException("annotated_methods contain duplicates");
        }
    }

    public void addMethodParameterAnnotations(MethodId method,
                                              AnnotationSetList annotations) {
        if (annotations.isEmpty()) {
            throw new IllegalStateException("parameter annotations is empty");
        }
        if (annotated_parameters.putIfAbsent(method, annotations) != null) {
            throw new IllegalStateException("annotated_parameters contain duplicates");
        }
    }

    public static AnnotationsDirectory read(RandomInput in, ReadContext context) {
        AnnotationsDirectory out = new AnnotationsDirectory();
        int class_annotations_off = in.readInt();
        if (class_annotations_off != 0) {
            RandomInput in2 = context.data(class_annotations_off);
            out.class_annotations = AnnotationSet.read(in2, context);
        } else {
            out.class_annotations = AnnotationSet.empty();
        }
        int annotated_fields_size = in.readInt();
        int annotated_methods_size = in.readInt();
        int annotated_parameters_size = in.readInt();
        out.annotated_fields = new HashMap<>(annotated_fields_size);
        for (int i = 0; i < annotated_fields_size; i++) {
            FieldId field = context.field(in.readInt());
            int field_annotations_off = in.readInt();
            RandomInput in2 = context.data(field_annotations_off);
            AnnotationSet field_annotations = AnnotationSet.read(in2, context);
            out.annotated_fields.put(field, field_annotations);
        }
        out.annotated_methods = new HashMap<>(annotated_methods_size);
        for (int i = 0; i < annotated_methods_size; i++) {
            MethodId method = context.method(in.readInt());
            int method_annotations_off = in.readInt();
            RandomInput in2 = context.data(method_annotations_off);
            AnnotationSet method_annotations = AnnotationSet.read(in2, context);
            out.annotated_methods.put(method, method_annotations);
        }
        out.annotated_parameters = new HashMap<>(annotated_parameters_size);
        for (int i = 0; i < annotated_parameters_size; i++) {
            MethodId method = context.method(in.readInt());
            int parameters_annotations_off = in.readInt();
            RandomInput in2 = context.data(parameters_annotations_off);
            AnnotationSetList parameters_annotations = AnnotationSetList.read(in2, context);
            out.annotated_parameters.put(method, parameters_annotations);
        }
        return out;
    }

    public void write(WriteContext context, RandomOutput out) {
        out.writeInt(class_annotations.isEmpty() ? 0
                : context.getAnnotationSetOffset(class_annotations));

        out.writeInt(annotated_fields.size());
        out.writeInt(annotated_methods.size());
        out.writeInt(annotated_parameters.size());

        FieldId[] fields = annotated_fields.keySet().toArray(new FieldId[0]);
        Arrays.sort(fields, FieldId.COMPARATOR);
        for (FieldId tmp : fields) {
            out.writeInt(context.getFieldIndex(tmp));
            out.writeInt(context.getAnnotationSetOffset(
                    annotated_fields.get(tmp)));
        }

        MethodId[] methods = annotated_methods.keySet().toArray(new MethodId[0]);
        Arrays.sort(methods, MethodId.COMPARATOR);
        for (MethodId tmp : methods) {
            out.writeInt(context.getMethodIndex(tmp));
            out.writeInt(context.getAnnotationSetOffset(
                    annotated_methods.get(tmp)));
        }

        methods = annotated_parameters.keySet().toArray(new MethodId[0]);
        Arrays.sort(methods, MethodId.COMPARATOR);
        for (MethodId tmp : methods) {
            out.writeInt(context.getMethodIndex(tmp));
            out.writeInt(context.getAnnotationSetListOffset(
                    annotated_parameters.get(tmp)));
        }
    }

    static void writeSection(WriteContextImpl context, FileMap map, RandomOutput out,
                             Map<TypeId, AnnotationsDirectory> annotations_directories) {
        //TODO: delete duplicates
        if (!annotations_directories.isEmpty()) {
            out.alignPosition(AnnotationsDirectory.ALIGNMENT);
            map.annotations_directories_off = (int) out.position();
            map.annotations_directories_size = 0;
            for (Map.Entry<TypeId, AnnotationsDirectory> tmp
                    : annotations_directories.entrySet()) {
                AnnotationsDirectory ad = tmp.getValue();
                if (!ad.isEmpty()) {
                    map.annotations_directories_size++;
                    out.alignPosition(AnnotationsDirectory.ALIGNMENT);
                    int start = (int) out.position();
                    ad.write(context, out);
                    context.addAnnotationsDirectory(tmp.getKey(), start);
                } else {
                    context.addAnnotationsDirectory(tmp.getKey(), 0);
                }
            }
        }
    }

    public boolean isEmpty() {
        return class_annotations.isEmpty() && annotated_fields.isEmpty()
                && annotated_methods.isEmpty() && annotated_parameters.isEmpty();
    }

    public static AnnotationsDirectory empty() {
        AnnotationsDirectory out = new AnnotationsDirectory();
        out.class_annotations = AnnotationSet.empty();
        out.annotated_fields = new HashMap<>(0);
        out.annotated_methods = new HashMap<>(0);
        out.annotated_parameters = new HashMap<>(0);
        return out;
    }
}
