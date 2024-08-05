package com.v7878.dex.base;

import com.v7878.dex.iface.TypeId;
import com.v7878.dex.util.CollectionUtils;

import java.util.Objects;

public abstract class BaseTypeId implements TypeId {
    @Override
    public int hashCode() {
        return Objects.hashCode(getDescriptor());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof TypeId other
                && Objects.equals(getDescriptor(), other.getDescriptor());
    }

    @Override
    public int compareTo(TypeId other) {
        if (other == this) return 0;
        return CollectionUtils.compareNonNull(getDescriptor(), other.getDescriptor());
    }
}
