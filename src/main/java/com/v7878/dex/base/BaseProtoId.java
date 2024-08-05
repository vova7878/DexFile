package com.v7878.dex.base;

import com.v7878.dex.iface.ProtoId;
import com.v7878.dex.util.CollectionUtils;

import java.util.Objects;

public abstract class BaseProtoId implements ProtoId {
    @Override
    public int hashCode() {
        return Objects.hash(getReturnType(), getParameterTypes());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof ProtoId other
                && Objects.equals(getReturnType(), other.getReturnType())
                && Objects.equals(getParameterTypes(), other.getParameterTypes());
    }

    @Override
    public int compareTo(ProtoId other) {
        if (other == this) return 0;
        int out = CollectionUtils.compareNonNull(getReturnType(), other.getReturnType());
        if (out != 0) return out;
        return CollectionUtils.compareLexicographically(getParameterTypes(), other.getParameterTypes());
    }
}
