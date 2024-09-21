package com.v7878.dex.immutable;

import com.v7878.dex.base.BaseAnnotationElement;
import com.v7878.dex.iface.AnnotationElement;
import com.v7878.dex.iface.value.EncodedValue;
import com.v7878.dex.immutable.value.ImmutableEncodedValue;
import com.v7878.dex.util.CollectionUtils;

import java.util.Objects;

public class ImmutableAnnotationElement extends BaseAnnotationElement {
    private final String name;
    private final ImmutableEncodedValue value;

    protected ImmutableAnnotationElement(String name, EncodedValue value) {
        this.name = name;
        this.value = ImmutableEncodedValue.of(value);
    }

    public static ImmutableAnnotationElement of(String name, EncodedValue value) {
        return new ImmutableAnnotationElement(name, value);
    }

    public static ImmutableAnnotationElement of(AnnotationElement other) {
        if (other instanceof ImmutableAnnotationElement immutable) return immutable;
        return new ImmutableAnnotationElement(other.getName(), other.getValue());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ImmutableEncodedValue getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof AnnotationElement other
                && Objects.equals(getName(), other.getName())
                && Objects.equals(getValue(), other.getValue());
    }

    @Override
    public int compareTo(AnnotationElement other) {
        if (other == this) return 0;
        return CollectionUtils.compareNonNull(getName(), other.getName());
    }
}
