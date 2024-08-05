package com.v7878.dex.base.value;

import com.v7878.dex.ValueType;
import com.v7878.dex.iface.value.EncodedByte;
import com.v7878.dex.iface.value.EncodedValue;

public abstract class BaseEncodedByte implements EncodedByte {
    @Override
    public int hashCode() {
        return Byte.hashCode(getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof EncodedByte other
                && getValue() == other.getValue();
    }

    @Override
    public int compareTo(EncodedValue other) {
        if (other == this) return 0;
        int out = ValueType.compare(getValueType(), other.getValueType());
        if (out != 0) return out;
        return Byte.compare(getValue(), ((EncodedByte) other).getValue());
    }
}
