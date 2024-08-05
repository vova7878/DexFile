package com.v7878.dex.base;

import com.v7878.dex.iface.Annotation;
import com.v7878.dex.util.CollectionUtils;

import java.util.Objects;

public abstract class BaseAnnotation implements Annotation {
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
