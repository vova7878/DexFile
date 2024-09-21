package com.v7878.dex.immutable;

import com.v7878.dex.immutable.value.EncodedValue;
import com.v7878.dex.util.CollectionUtils;

import java.util.Objects;

public final class AnnotationElement implements Comparable<AnnotationElement> {
    private final String name;
    private final EncodedValue value;

    private AnnotationElement(String name, EncodedValue value) {
        this.name = name;
        this.value = EncodedValue.of(value);
    }

    public static AnnotationElement of(String name, EncodedValue value) {
        return new AnnotationElement(name, value);
    }

    public String getName() {
        return name;
    }

    public EncodedValue getValue() {
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
