package com.v7878.dex.immutable.value;

import com.v7878.dex.Internal;
import com.v7878.dex.ValueType;
import com.v7878.dex.immutable.AnnotationElement;
import com.v7878.dex.immutable.CommonAnnotation;
import com.v7878.dex.immutable.TypeId;
import com.v7878.dex.util.CollectionUtils;
import com.v7878.dex.util.Converter;

import java.util.NavigableSet;
import java.util.Objects;

public final class EncodedAnnotation extends EncodedValue implements CommonAnnotation {
    private final TypeId type;
    private final NavigableSet<AnnotationElement> elements;

    private EncodedAnnotation(TypeId type, NavigableSet<AnnotationElement> elements) {
        this.type = Objects.requireNonNull(type);
        this.elements = Objects.requireNonNull(elements);
    }

    @Internal
    public static EncodedAnnotation raw(TypeId type, NavigableSet<AnnotationElement> elements) {
        return new EncodedAnnotation(type, elements);
    }

    public static EncodedAnnotation of(TypeId type, Iterable<AnnotationElement> elements) {
        return new EncodedAnnotation(type, Converter.toNavigableSet(elements));
    }

    public static EncodedAnnotation of(CommonAnnotation other) {
        if (other instanceof EncodedAnnotation encoded) return encoded;
        return new EncodedAnnotation(other.getType(), other.getElements());
    }

    @Override
    public ValueType getValueType() {
        return ValueType.ANNOTATION;
    }

    @Override
    public boolean isDefault() {
        return false;
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
        return Objects.hash(getType(), getElements());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof EncodedAnnotation other
                && Objects.equals(getType(), other.getType())
                && Objects.equals(getElements(), other.getElements());
    }

    @Override
    public int compareTo(EncodedValue other) {
        if (other == this) return 0;
        int out = ValueType.compare(getValueType(), other.getValueType());
        if (out != 0) return out;
        var other_anno = ((EncodedAnnotation) other);
        out = CollectionUtils.compareNonNull(getType(), other_anno.getType());
        if (out != 0) return out;
        return CollectionUtils.compareLexicographically(
                getElements(), other_anno.getElements());
    }
}
