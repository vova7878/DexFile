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

import com.v7878.dex.EncodedValue.ArrayValue;
import com.v7878.dex.io.RandomInput;
import com.v7878.dex.io.RandomOutput;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class ClassDef implements Mutable {

    public static final int SIZE = 0x20;

    private static void add(Map<TypeId, ClassDef> map,
                            Set<ClassDef> added, ArrayList<ClassDef> out, TypeId type) {
        ClassDef value = map.get(type);
        if (value == null) {
            return;
        }
        if (added.contains(value)) {
            return;
        }
        if (value.superclass != null) {
            add(map, added, out, value.superclass);
        }
        for (TypeId tmp : value.interfaces) {
            add(map, added, out, tmp);
        }
        out.add(value);
        added.add(value);
    }

    // TODO: cycle test
    public static ClassDef[] sort(List<ClassDef> class_defs) {
        Map<TypeId, ClassDef> map = new HashMap<>();
        class_defs.forEach((value) -> {
            if (map.putIfAbsent(value.clazz, value) != null) {
                throw new IllegalStateException(
                        "class defs contain duplicates: " + value.clazz);
            }
        });

        Set<ClassDef> added = new HashSet<>();
        ArrayList<ClassDef> out = new ArrayList<>(class_defs.size());

        class_defs.forEach(value -> add(map, added, out, value.clazz));

        if (out.size() != class_defs.size()) {
            throw new IllegalStateException("sorted.length(" +
                    out.size() + ") != input.length(" + class_defs.size() + ")");
        }
        return out.toArray(new ClassDef[0]);
    }

    private TypeId clazz;
    private int access_flags;
    private TypeId superclass;
    private TypeList interfaces;
    private String source_file;
    private AnnotationSet annotations;
    private ClassData class_data;

    public ClassDef(TypeId clazz) {
        setType(clazz);
        setAccessFlags(0);
        setSuperClass(null);
        setInterfaces(null);
        setSourceFile(null);
        setAnnotations(null);
        setClassData(null);
    }

    public ClassDef(TypeId clazz, int access_flags, TypeId superclass,
                    Collection<TypeId> interfaces, String source_file,
                    Collection<AnnotationItem> annotations, ClassData class_data) {
        setType(clazz);
        setAccessFlags(access_flags);
        setSuperClass(superclass);
        setInterfaces(interfaces);
        setSourceFile(source_file);
        setAnnotations(annotations);
        setClassData(class_data);
    }

    public void setType(TypeId clazz) {
        this.clazz = Objects.requireNonNull(clazz,
                "type can`n be null").mutate();
    }

    public TypeId getType() {
        return clazz;
    }

    public void setAccessFlags(int access_flags) {
        this.access_flags = access_flags;
    }

    public int getAccessFlags() {
        return access_flags;
    }

    public void setSuperClass(TypeId superclass) {
        this.superclass = superclass == null ? null : superclass.mutate();
    }

    public TypeId getSuperClass() {
        return superclass;
    }

    public void setInterfaces(Collection<TypeId> interfaces) {
        this.interfaces = interfaces == null
                ? TypeList.empty() : new TypeList(interfaces);
    }

    public TypeList getInterfaces() {
        return interfaces;
    }

    public void setSourceFile(String source_file) {
        this.source_file = source_file;
    }

    public String getSourceFile() {
        return source_file;
    }

    public void setAnnotations(Collection<AnnotationItem> annotations) {
        this.annotations = annotations == null
                ? AnnotationSet.empty() : new AnnotationSet(annotations);
    }

    public AnnotationSet getAnnotations() {
        return annotations;
    }

    public void setClassData(ClassData class_data) {
        this.class_data = class_data == null
                ? ClassData.empty() : class_data.mutate();
    }

    public ClassData getClassData() {
        return class_data;
    }

    public static ClassDef read(RandomInput in, ReadContext context) {
        TypeId clazz = context.type(in.readInt());
        int access_flags = in.readInt();
        int superclass_idx = in.readInt();
        TypeId superclass = null;
        if (superclass_idx != DexConstants.NO_INDEX) {
            superclass = context.type(superclass_idx);
        }
        int interfaces_off = in.readInt();
        TypeList interfaces = null;
        if (interfaces_off != 0) {
            interfaces = TypeList.read(context.data(interfaces_off), context);
        }
        int source_file_idx = in.readInt();
        String source_file = null;
        if (source_file_idx != DexConstants.NO_INDEX) {
            source_file = context.string(source_file_idx);
        }
        int annotations_off = in.readInt();
        AnnotationsDirectory annotations = AnnotationsDirectory.empty();
        if (annotations_off != 0) {
            RandomInput in2 = context.data(annotations_off);
            annotations = AnnotationsDirectory.read(in2, context);
        }
        AnnotationSet class_annotations = annotations.class_annotations;
        int class_data_off = in.readInt();
        ArrayValue static_values = new ArrayValue();
        int static_values_off = in.readInt();
        if (static_values_off != 0) {
            RandomInput in2 = context.data(static_values_off);
            static_values = (ArrayValue) EncodedValue
                    .readValue(in2, context, EncodedValue.EncodedValueType.ARRAY);
        }
        ClassData class_data = null;
        if (class_data_off != 0) {
            class_data = ClassData.read(context.data(class_data_off),
                    context, static_values, annotations.annotated_fields,
                    annotations.annotated_methods, annotations.annotated_parameters);
        }
        return new ClassDef(clazz, access_flags, superclass, interfaces,
                source_file, class_annotations, class_data);
    }

    static ClassDef[] readArray(RandomInput in, ReadContext context, int[] ids) {
        ClassDef[] out = new ClassDef[ids.length];
        for (int i = 0; i < ids.length; i++) {
            int id = ids[i];
            if (id < 0) {
                out[i] = out[~id];
            } else {
                RandomInput tmp = in.duplicate(in.position());
                tmp.addPosition((long) id * ClassDef.SIZE);
                out[i] = ClassDef.read(in, context);
            }
        }
        return out;
    }

    private ArrayValue getStaticFieldValues() {
        ArrayValue out = new ArrayValue();
        EncodedValue[] tmp = class_data.getStaticFields().stream()
                .map(EncodedField::getValue).toArray(EncodedValue[]::new);
        if (tmp.length == 0) {
            return out;
        }
        int size = tmp.length;
        for (; size > 0; size--) {
            if (!tmp[size - 1].isDefault()) {
                break;
            }
        }
        out.addAll(tmp, 0, size);
        return out;
    }

    public void collectData(DataCollector data) {
        data.add(clazz);
        if (superclass != null) {
            data.add(superclass);
        }
        if (!interfaces.isEmpty()) {
            data.add(interfaces);
        }
        if (source_file != null) {
            data.add(source_file);
        }
        if (!annotations.isEmpty()) {
            data.add(annotations);
        }
        if (!class_data.isEmpty()) {
            data.add(class_data);
        }
        ArrayValue static_values = getStaticFieldValues();
        if (!static_values.containsOnlyDefaults()) {
            data.add(static_values);
        }
    }

    // TODO: check that if default methods exist then the targetApi is higher than 23 (dex 037+)

    public void write(WriteContext context, RandomOutput out) {
        out.writeInt(context.getTypeIndex(clazz));
        out.writeInt(access_flags);
        out.writeInt(superclass == null ? DexConstants.NO_INDEX : context.getTypeIndex(superclass));
        out.writeInt(interfaces.isEmpty() ? 0 : context.getTypeListOffset(interfaces));
        out.writeInt(source_file == null ? DexConstants.NO_INDEX : context.getStringIndex(source_file));
        out.writeInt(context.getAnnotationsDirectoryOffset(clazz));
        out.writeInt(class_data.isEmpty() ? 0 : context.getClassDataOffset(class_data));
        ArrayValue static_values = getStaticFieldValues();
        out.writeInt(static_values.containsOnlyDefaults() ? 0
                : context.getArrayValueOffset(static_values));
    }

    static void writeSection(WriteContext context, RandomOutput out, ClassDef[] classDefs) {
        for (ClassDef value : classDefs) {
            value.write(context, out);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof ClassDef cdobj
                && access_flags == cdobj.access_flags
                && Objects.equals(clazz, cdobj.clazz)
                && Objects.equals(superclass, cdobj.superclass)
                && Objects.equals(interfaces, cdobj.interfaces)
                && Objects.equals(source_file, cdobj.source_file)
                && Objects.equals(annotations, cdobj.annotations)
                && Objects.equals(class_data, cdobj.class_data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clazz, access_flags, superclass,
                interfaces, source_file, annotations, class_data);
    }

    @Override
    public ClassDef mutate() {
        return new ClassDef(clazz, access_flags, superclass, interfaces,
                source_file, annotations, class_data);
    }
}
