package com.v7878.dex.base;

import com.v7878.dex.MethodHandleType;
import com.v7878.dex.iface.FieldId;
import com.v7878.dex.iface.MethodHandleId;
import com.v7878.dex.iface.MethodId;
import com.v7878.dex.util.CollectionUtils;

import java.util.Objects;

public abstract class BaseMethodHandleId implements MethodHandleId {
    @Override
    public int hashCode() {
        return Objects.hash(getHandleType(), getMember());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof MethodHandleId other
                && Objects.equals(getHandleType(), other.getHandleType())
                && Objects.equals(getMember(), other.getMember());
    }

    @Override
    public int compareTo(MethodHandleId other) {
        if (other == this) return 0;
        int out = MethodHandleType.compare(getHandleType(), other.getHandleType());
        if (out != 0) return out;
        if (getHandleType().isMethodAccess()) {
            return CollectionUtils.compareNonNull((MethodId) getMember(), (MethodId) other.getMember());
        } else {
            return CollectionUtils.compareNonNull((FieldId) getMember(), (FieldId) other.getMember());
        }
    }
}
