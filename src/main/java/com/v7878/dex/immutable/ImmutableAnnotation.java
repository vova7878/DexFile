package com.v7878.dex.immutable;

import com.v7878.dex.AnnotationVisibility;
import com.v7878.dex.base.BaseAnnotation;
import com.v7878.dex.iface.Annotation;
import com.v7878.dex.iface.AnnotationElement;
import com.v7878.dex.iface.TypeId;
import com.v7878.dex.iface.value.EncodedAnnotation;
import com.v7878.dex.util.CollectionUtils;
import com.v7878.dex.util.ItemConverter;

import java.util.NavigableSet;
import java.util.Objects;

public class ImmutableAnnotation extends BaseAnnotation {
    private final AnnotationVisibility visibility;
    private final ImmutableTypeId type;
    private final NavigableSet<? extends ImmutableAnnotationElement> elements;

    protected ImmutableAnnotation(AnnotationVisibility visibility, TypeId type,
                                  Iterable<? extends AnnotationElement> elements) {
        this.visibility = Objects.requireNonNull(visibility);
        this.type = ImmutableTypeId.of(type);
        this.elements = ItemConverter.toNavigableSet(ImmutableAnnotationElement::of,
                value -> value instanceof ImmutableAnnotationElement, elements);
    }

    public static ImmutableAnnotation of(AnnotationVisibility visibility, TypeId type,
                                         Iterable<? extends AnnotationElement> elements) {
        return new ImmutableAnnotation(visibility, type, elements);
    }

    public static ImmutableAnnotation of(AnnotationVisibility visibility, EncodedAnnotation annotation) {
        return new ImmutableAnnotation(visibility, annotation.getType(), annotation.getElements());
    }

    public static ImmutableAnnotation of(Annotation other) {
        if (other instanceof ImmutableAnnotation immutable) return immutable;
        return new ImmutableAnnotation(other.getVisibility(), other.getType(), other.getElements());
    }

    @Override
    public AnnotationVisibility getVisibility() {
        return visibility;
    }

    @Override
    public ImmutableTypeId getType() {
        return type;
    }

    @Override
    public NavigableSet<? extends ImmutableAnnotationElement> getElements() {
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
