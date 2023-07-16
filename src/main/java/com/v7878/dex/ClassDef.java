package com.v7878.dex;

import com.v7878.dex.io.RandomInput;
import com.v7878.dex.io.RandomOutput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ClassDef implements PublicCloneable {

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
                    TypeList interfaces, String source_file,
                    AnnotationSet annotations, ClassData class_data) {
        setType(clazz);
        setAccessFlags(access_flags);
        setSuperClass(superclass);
        setInterfaces(interfaces);
        setSourceFile(source_file);
        setAnnotations(annotations);
        setClassData(class_data);
    }

    public final void setType(TypeId clazz) {
        this.clazz = Objects.requireNonNull(clazz,
                "type can`n be null").clone();
    }

    public final TypeId getType() {
        return clazz;
    }

    public final void setAccessFlags(int access_flags) {
        this.access_flags = access_flags;
    }

    public final int getAccessFlags() {
        return access_flags;
    }

    public final void setSuperClass(TypeId superclass) {
        this.superclass = superclass == null ? null : superclass.clone();
    }

    public final TypeId getSuperClass() {
        return superclass;
    }

    public final void setInterfaces(TypeList interfaces) {
        this.interfaces = interfaces == null
                ? TypeList.empty() : interfaces.clone();
    }

    public final TypeList getInterfaces() {
        return interfaces;
    }

    public final void setSourceFile(String source_file) {
        this.source_file = source_file;
    }

    public final String getSourceFile() {
        return source_file;
    }

    public final void setAnnotations(AnnotationSet annotations) {
        this.annotations = annotations == null
                ? AnnotationSet.empty() : annotations.clone();
    }

    public final AnnotationSet getAnnotations() {
        return annotations;
    }

    public final void setClassData(ClassData class_data) {
        this.class_data = class_data == null
                ? ClassData.empty() : class_data.clone();
    }

    public final ClassData getClassData() {
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
            interfaces = TypeList.read(in.duplicate(interfaces_off), context);
        }
        int source_file_idx = in.readInt();
        String source_file = null;
        if (source_file_idx != DexConstants.NO_INDEX) {
            source_file = context.string(source_file_idx);
        }
        int annotations_off = in.readInt();
        AnnotationsDirectory annotations = AnnotationsDirectory.empty();
        if (annotations_off != 0) {
            RandomInput in2 = in.duplicate(annotations_off);
            annotations = AnnotationsDirectory.read(in2, context);
        }
        AnnotationSet class_annotations = annotations.class_annotations;
        int class_data_off = in.readInt();
        EncodedValue.ArrayValue static_values = new EncodedValue.ArrayValue();
        int static_values_off = in.readInt();
        if (static_values_off != 0) {
            RandomInput in2 = in.duplicate(static_values_off);
            static_values = (EncodedValue.ArrayValue) EncodedValueReader
                    .readValue(in2, context, EncodedValue.EncodedValueType.ARRAY);
        }
        ClassData class_data = null;
        if (class_data_off != 0) {
            class_data = ClassData.read(in.duplicate(class_data_off),
                    context, static_values, annotations);
        }
        return new ClassDef(clazz, access_flags, superclass, interfaces,
                source_file, class_annotations, class_data);
    }

    private EncodedValue.ArrayValue getStaticFieldValues() {
        EncodedValue.ArrayValue out = new EncodedValue.ArrayValue();
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
        AnnotationsDirectory all_annotations = AnnotationsDirectory.empty();
        all_annotations.setClassAnnotations(annotations);
        if (!class_data.isEmpty()) {
            data.add(class_data);
            class_data.fillAnnotations(all_annotations);
        }
        data.add(this, all_annotations);
        EncodedValue.ArrayValue static_values = getStaticFieldValues();
        if (!static_values.containsOnlyDefaults()) {
            data.add(static_values);
        }
    }

    public void write(WriteContext context, RandomOutput out) {
        out.writeInt(context.getTypeIndex(clazz));
        out.writeInt(access_flags);
        out.writeInt(superclass == null ? DexConstants.NO_INDEX : context.getTypeIndex(superclass));
        out.writeInt(interfaces.isEmpty() ? 0 : context.getTypeListOffset(interfaces));
        out.writeInt(source_file == null ? DexConstants.NO_INDEX : context.getStringIndex(source_file));
        out.writeInt(context.getAnnotationsDirectoryOffset(this));
        out.writeInt(class_data.isEmpty() ? 0 : context.getClassDataOffset(class_data));
        EncodedValue.ArrayValue static_values = getStaticFieldValues();
        out.writeInt(static_values.containsOnlyDefaults() ? 0
                : context.getArrayValueOffset(static_values));
    }

    @Override
    public ClassDef clone() {
        return new ClassDef(clazz, access_flags, superclass, interfaces,
                source_file, annotations, class_data);
    }
}
