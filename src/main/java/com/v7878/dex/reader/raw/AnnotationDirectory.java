package com.v7878.dex.reader.raw;

import static com.v7878.dex.DexConstants.NO_OFFSET;

import com.v7878.dex.io.RandomInput;
import com.v7878.dex.reader.ReaderAnnotation;
import com.v7878.dex.reader.ReaderDex;
import com.v7878.dex.reader.util.OptionalUtils;
import com.v7878.dex.util.SparseArray;

import java.util.Set;

public class AnnotationDirectory {
    public static final AnnotationDirectory EMPTY = new AnnotationDirectory();

    public static final int CLASS_ANNOTATIONS_OFFSET = 0;
    public static final int FIELDS_SIZE_OFFSET = 4;
    public static final int METHODS_SIZE_OFFSET = 8;
    public static final int PARAMETERS_SIZE_OFFSET = 12;
    public static final int ENTRIES_OFFSET = PARAMETERS_SIZE_OFFSET + Integer.BYTES;
    public static final int ENTRY_SIZE = 8;

    private final ReaderDex dexfile;
    private final int offset;
    private final int fields_size;
    private final int methods_size;
    private final int parameters_size;

    public AnnotationDirectory(ReaderDex dexfile, int offset) {
        this.dexfile = dexfile;
        this.offset = offset;
        this.fields_size = dexfile.dataAt(offset + FIELDS_SIZE_OFFSET).readSmallUInt();
        this.methods_size = dexfile.dataAt(offset + METHODS_SIZE_OFFSET).readSmallUInt();
        this.parameters_size = dexfile.dataAt(offset + PARAMETERS_SIZE_OFFSET).readSmallUInt();
    }

    private Set<ReaderAnnotation> class_annotations;
    private SparseArray<Set<ReaderAnnotation>> field_annotations;
    private SparseArray<Set<ReaderAnnotation>> method_annotations;
    private SparseArray<AnnotationSetList> parameter_annotations;

    private AnnotationDirectory() {
        this.dexfile = null;
        this.offset = 0;
        this.fields_size = 0;
        this.methods_size = 0;
        this.parameters_size = 0;
        this.class_annotations = Set.of();
    }

    public Set<ReaderAnnotation> getClassAnnotations() {
        if (class_annotations != null) return class_annotations;
        return class_annotations = OptionalUtils.getOrDefault(
                dexfile.dataAt(offset + CLASS_ANNOTATIONS_OFFSET).readSmallUInt(),
                NO_OFFSET, dexfile::getAnnotationSet, Set.of());
    }

    public SparseArray<Set<ReaderAnnotation>> getFieldAnnotations() {
        if (field_annotations != null) return field_annotations;
        SparseArray<Set<ReaderAnnotation>> out = new SparseArray<>();
        if (fields_size == 0) return field_annotations = out;
        RandomInput in = dexfile.dataAt(offset + ENTRIES_OFFSET);
        for (int i = 0; i < fields_size; i++) {
            int field_idx = in.readSmallUInt();
            int annotations_off = in.readSmallUInt();
            out.put(field_idx, dexfile.getAnnotationSet(annotations_off));
        }
        return field_annotations = out;
    }

    public SparseArray<Set<ReaderAnnotation>> getMethodAnnotations() {
        if (method_annotations != null) return method_annotations;
        SparseArray<Set<ReaderAnnotation>> out = new SparseArray<>();
        if (methods_size == 0) return method_annotations = out;
        RandomInput in = dexfile.dataAt(offset +
                ENTRIES_OFFSET + fields_size * ENTRY_SIZE);
        for (int i = 0; i < methods_size; i++) {
            int method_idx = in.readSmallUInt();
            int annotations_off = in.readSmallUInt();
            out.put(method_idx, dexfile.getAnnotationSet(annotations_off));
        }
        return method_annotations = out;
    }

    public SparseArray<AnnotationSetList> getParameterAnnotations() {
        if (parameter_annotations != null) return parameter_annotations;
        SparseArray<AnnotationSetList> out = new SparseArray<>();
        if (parameters_size == 0) return parameter_annotations = out;
        RandomInput in = dexfile.dataAt(offset + ENTRIES_OFFSET
                + (fields_size + methods_size) * ENTRY_SIZE);
        for (int i = 0; i < parameters_size; i++) {
            int method_idx = in.readSmallUInt();
            int annotations_off = in.readSmallUInt();
            out.put(method_idx, dexfile.getAnnotationSetList(annotations_off));
        }
        return parameter_annotations = out;
    }
}
