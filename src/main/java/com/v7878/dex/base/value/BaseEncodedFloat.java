package com.v7878.dex.base.value;

import com.v7878.dex.ValueType;
import com.v7878.dex.iface.value.EncodedFloat;
import com.v7878.dex.iface.value.EncodedValue;

public abstract class BaseEncodedFloat implements EncodedFloat {
    @Override
    public int hashCode() {
        return Float.hashCode(getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof EncodedFloat other
                && getValue() == other.getValue();
    }

    @Override
    public int compareTo(EncodedValue other) {
        if (other == this) return 0;
        int out = ValueType.compare(getValueType(), other.getValueType());
        if (out != 0) return out;
        return Float.compare(getValue(), ((EncodedFloat) other).getValue());
    }
}
