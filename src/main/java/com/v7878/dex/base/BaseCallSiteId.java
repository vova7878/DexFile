package com.v7878.dex.base;

import com.v7878.dex.iface.CallSiteId;
import com.v7878.dex.util.CollectionUtils;

import java.util.Objects;

public abstract class BaseCallSiteId implements CallSiteId {
    @Override
    public int hashCode() {
        return Objects.hash(getMethodName(), getMethodProto(), getMethodHandle(), getExtraArguments());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof CallSiteId other
                && Objects.equals(getMethodName(), other.getMethodName())
                && Objects.equals(getMethodProto(), other.getMethodProto())
                && Objects.equals(getMethodHandle(), other.getMethodHandle())
                && Objects.equals(getExtraArguments(), other.getExtraArguments());
    }

    @Override
    public int compareTo(CallSiteId other) {
        if (other == this) return 0;
        int out = CollectionUtils.compareNonNull(getMethodHandle(), other.getMethodHandle());
        if (out != 0) return out;
        out = CollectionUtils.compareNonNull(getMethodName(), other.getMethodName());
        if (out != 0) return out;
        out = CollectionUtils.compareNonNull(getMethodProto(), other.getMethodProto());
        if (out != 0) return out;
        return CollectionUtils.compareLexicographically(getExtraArguments(), other.getExtraArguments());
    }
}
