package com.v7878.dex.reader.raw;

import static com.v7878.dex.DexConstants.NO_OFFSET;

import com.v7878.dex.reader.ReaderAnnotation;
import com.v7878.dex.reader.ReaderDex;
import com.v7878.dex.reader.util.OptionalUtils;
import com.v7878.dex.util.SparseArray;

import java.util.Set;

public class AnnotationDirectory {
    public static final AnnotationDirectory EMPTY = new AnnotationDirectory(null, 0) {
        public Set<ReaderAnnotation> getClassAnnotations() {
            return Set.of();
        }
    };

    public static final int CLASS_ANNOTATIONS_OFFSET = 0;
    public static final int FIELDS_SIZE_OFFSET = 4;
    public static final int METHODS_SIZE_OFFSET = 8;
    public static final int PARAMETERS_SIZE_OFFSET = 12;

    private final ReaderDex dexfile;
    private final int offset;

    public AnnotationDirectory(ReaderDex dexfile, int offset) {
        this.dexfile = dexfile;
        this.offset = offset;
    }

    private Set<ReaderAnnotation> class_annotations;

    public Set<ReaderAnnotation> getClassAnnotations() {
        if (class_annotations != null) return class_annotations;
        return class_annotations = OptionalUtils.getOrDefault(
                dexfile.mainAt(offset + CLASS_ANNOTATIONS_OFFSET).readSmallUInt(),
                NO_OFFSET, dexfile::getAnnotationSet, Set.of());
    }

    public SparseArray<Set<ReaderAnnotation>> getFieldAnnotations() {
        // TODO
        throw new UnsupportedOperationException();
    }

    public SparseArray<Set<ReaderAnnotation>> getMethodAnnotations() {
        // TODO
        throw new UnsupportedOperationException();
    }

    public SparseArray<AnnotationSetList> getParameterAnnotations() {
        // TODO
        throw new UnsupportedOperationException();
    }
}
