package com.v7878.dex.base.value;

import com.v7878.dex.ValueType;
import com.v7878.dex.iface.value.EncodedBoolean;
import com.v7878.dex.iface.value.EncodedValue;

public abstract class BaseEncodedBoolean implements EncodedBoolean {
    @Override
    public int hashCode() {
        return Boolean.hashCode(getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof EncodedBoolean other
                && getValue() == other.getValue();
    }

    @Override
    public int compareTo(EncodedValue other) {
        if (other == this) return 0;
        int out = ValueType.compare(getValueType(), other.getValueType());
        if (out != 0) return out;
        return Boolean.compare(getValue(), ((EncodedBoolean) other).getValue());
    }
}
