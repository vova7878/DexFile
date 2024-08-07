package com.v7878.dex.immutable.value;

import com.v7878.dex.base.value.BaseEncodedAnnotation;
import com.v7878.dex.iface.AnnotationElement;
import com.v7878.dex.iface.TypeId;
import com.v7878.dex.iface.value.EncodedAnnotation;
import com.v7878.dex.immutable.ImmutableAnnotationElement;
import com.v7878.dex.immutable.ImmutableTypeId;
import com.v7878.dex.util.ItemConverter;

import java.util.NavigableSet;

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

    public static ImmutableEncodedAnnotation of(EncodedAnnotation other) {
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
}
