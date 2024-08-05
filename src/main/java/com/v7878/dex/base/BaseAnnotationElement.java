package com.v7878.dex.base;

import com.v7878.dex.iface.AnnotationElement;
import com.v7878.dex.util.CollectionUtils;

import java.util.Objects;

public abstract class BaseAnnotationElement implements AnnotationElement {
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
