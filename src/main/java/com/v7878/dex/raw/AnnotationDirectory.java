package com.v7878.dex.raw;

import com.v7878.dex.immutable.Annotation;
import com.v7878.dex.util.SparseArray;

import java.util.List;
import java.util.Set;

public record AnnotationDirectory(Set<Annotation> class_annotations,
                                  SparseArray<Set<Annotation>> field_annotations,
                                  SparseArray<Set<Annotation>> method_annotations,
                                  SparseArray<List<Set<Annotation>>> parameter_annotations) {
    @SuppressWarnings("rawtypes")
    private static final SparseArray EMPTY_ARRAY = new SparseArray();
    @SuppressWarnings("unchecked")
    public static final AnnotationDirectory EMPTY = new AnnotationDirectory(
            Set.of(), EMPTY_ARRAY, EMPTY_ARRAY, EMPTY_ARRAY);

    @SuppressWarnings("unchecked")
    public static AnnotationDirectory of(Set<Annotation> class_annotations,
                                         SparseArray<Set<Annotation>> field_annotations,
                                         SparseArray<Set<Annotation>> method_annotations,
                                         SparseArray<List<Set<Annotation>>> parameter_annotations) {
        return new AnnotationDirectory(
                class_annotations == null ? Set.of() : class_annotations,
                field_annotations == null ? EMPTY_ARRAY : field_annotations,
                method_annotations == null ? EMPTY_ARRAY : method_annotations,
                parameter_annotations == null ? EMPTY_ARRAY : parameter_annotations);
    }
}
