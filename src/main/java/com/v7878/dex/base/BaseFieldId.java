package com.v7878.dex.base;

import com.v7878.dex.iface.FieldId;
import com.v7878.dex.util.CollectionUtils;

import java.util.Objects;

public abstract class BaseFieldId implements FieldId {
    @Override
    public int hashCode() {
        return Objects.hash(getDeclaringClass(), getName(), getType());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof FieldId other
                && Objects.equals(getDeclaringClass(), other.getDeclaringClass())
                && Objects.equals(getName(), other.getName())
                && Objects.equals(getType(), other.getType());
    }

    @Override
    public int compareTo(FieldId other) {
        if (other == this) return 0;
        int out = CollectionUtils.compareNonNull(getDeclaringClass(), other.getDeclaringClass());
        if (out != 0) return out;
        out = CollectionUtils.compareNonNull(getName(), other.getName());
        if (out != 0) return out;
        return CollectionUtils.compareNonNull(getType(), other.getType());
    }
}
