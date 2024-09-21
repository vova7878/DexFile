package com.v7878.dex.immutable;

import com.v7878.dex.AnnotationVisibility;
import com.v7878.dex.immutable.value.EncodedAnnotation;
import com.v7878.dex.util.CollectionUtils;
import com.v7878.dex.util.ItemConverter;

import java.util.NavigableSet;
import java.util.Objects;

public class Annotation implements CommonAnnotation, Comparable<Annotation> {
    private final AnnotationVisibility visibility;
    private final TypeId type;
    private final NavigableSet<AnnotationElement> elements;

    protected Annotation(AnnotationVisibility visibility, TypeId type,
                         Iterable<AnnotationElement> elements) {
        this.visibility = Objects.requireNonNull(visibility);
        this.type = Objects.requireNonNull(type);
        this.elements = ItemConverter.toNavigableSet(elements);
    }

    public static Annotation of(AnnotationVisibility visibility, TypeId type,
                                Iterable<AnnotationElement> elements) {
        return new Annotation(visibility, type, elements);
    }

    public static Annotation of(AnnotationVisibility visibility, EncodedAnnotation annotation) {
        return new Annotation(visibility, annotation.getType(), annotation.getElements());
    }

    public AnnotationVisibility getVisibility() {
        return visibility;
    }

    @Override
    public TypeId getType() {
        return type;
    }

    @Override
    public NavigableSet<AnnotationElement> getElements() {
        return elements;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getVisibility(), getType(), getElements());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Annotation other
                && Objects.equals(getVisibility(), other.getVisibility())
                && Objects.equals(getType(), other.getType())
                && Objects.equals(getElements(), other.getElements());
    }

    @Override
    public int compareTo(Annotation other) {
        if (other == this) return 0;
        return CollectionUtils.compareNonNull(getType(), other.getType());
    }
}
