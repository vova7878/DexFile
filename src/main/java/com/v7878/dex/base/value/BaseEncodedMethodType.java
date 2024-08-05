package com.v7878.dex.base.value;

import com.v7878.dex.ValueType;
import com.v7878.dex.iface.value.EncodedMethodType;
import com.v7878.dex.iface.value.EncodedValue;
import com.v7878.dex.util.CollectionUtils;

import java.util.Objects;

public abstract class BaseEncodedMethodType implements EncodedMethodType {
    @Override
    public int hashCode() {
        return Objects.hashCode(getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof EncodedMethodType other
                && Objects.equals(getValue(), other.getValue());
    }

    @Override
    public int compareTo(EncodedValue other) {
        if (other == this) return 0;
        int out = ValueType.compare(getValueType(), other.getValueType());
        if (out != 0) return out;
        return CollectionUtils.compareNonNull(getValue(), ((EncodedMethodType) other).getValue());
    }
}
