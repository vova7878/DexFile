package com.v7878.dex.base.value;

import com.v7878.dex.ValueType;
import com.v7878.dex.iface.value.EncodedChar;
import com.v7878.dex.iface.value.EncodedValue;

public abstract class BaseEncodedChar implements EncodedChar {
    @Override
    public int hashCode() {
        return Character.hashCode(getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof EncodedChar other
                && getValue() == other.getValue();
    }

    @Override
    public int compareTo(EncodedValue other) {
        if (other == this) return 0;
        int out = ValueType.compare(getValueType(), other.getValueType());
        if (out != 0) return out;
        return Character.compare(getValue(), ((EncodedChar) other).getValue());
    }
}
