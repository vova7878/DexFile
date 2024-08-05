package com.v7878.dex.base.value;

import com.v7878.dex.ValueType;
import com.v7878.dex.iface.value.EncodedNull;
import com.v7878.dex.iface.value.EncodedValue;

public abstract class BaseEncodedNull implements EncodedNull {
    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof EncodedNull;
    }

    @Override
    public int compareTo(EncodedValue other) {
        if (other == this) return 0;
        return ValueType.compare(getValueType(), other.getValueType());
    }
}
