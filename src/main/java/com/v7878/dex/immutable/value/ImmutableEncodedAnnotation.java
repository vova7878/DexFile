package com.v7878.dex.immutable.value;

import com.v7878.dex.ValueType;
import com.v7878.dex.base.value.BaseEncodedAnnotation;
import com.v7878.dex.iface.AnnotationElement;
import com.v7878.dex.iface.CommonAnnotation;
import com.v7878.dex.iface.TypeId;
import com.v7878.dex.iface.value.EncodedAnnotation;
import com.v7878.dex.iface.value.EncodedValue;
import com.v7878.dex.immutable.ImmutableAnnotationElement;
import com.v7878.dex.immutable.ImmutableTypeId;
import com.v7878.dex.util.CollectionUtils;
import com.v7878.dex.util.ItemConverter;

import java.util.NavigableSet;
import java.util.Objects;

public class ImmutableEncodedAnnotation extends BaseEncodedAnnotation implements ImmutableEncodedValue {
    private final ImmutableTypeId type;
    private final NavigableSet<? extends ImmutableAnnotationElement> elements;

    protected ImmutableEncodedAnnotation(TypeId type, Iterable<? extends AnnotationElement> elements) {
        this.type = ImmutableTypeId.of(type);
        this.elements = ItemConverter.toNavigableSet(ImmutableAnnotationElement::of,
                value -> value instanceof ImmutableAnnotationElement, elements);
    }

    public static ImmutableEncodedAnnotation of(TypeId type, Iterable<? extends AnnotationElement> elements) {
        return new ImmutableEncodedAnnotation(type, elements);
    }

    public static ImmutableEncodedAnnotation of(CommonAnnotation other) {
        if (other instanceof ImmutableEncodedAnnotation immutable) return immutable;
        return new ImmutableEncodedAnnotation(other.getType(), other.getElements());
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
