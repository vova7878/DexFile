package com.v7878.dex.immutable.value;

import com.v7878.dex.ValueType;
import com.v7878.dex.base.value.BaseEncodedShort;
import com.v7878.dex.iface.value.EncodedShort;
import com.v7878.dex.iface.value.EncodedValue;

public class ImmutableEncodedShort extends BaseEncodedShort implements ImmutableEncodedValue {
    private final short value;

    protected ImmutableEncodedShort(short value) {
        this.value = value;
    }

    public static ImmutableEncodedShort of(short value) {
        return new ImmutableEncodedShort(value);
    }

    public static ImmutableEncodedShort of(EncodedShort other) {
        if (other instanceof ImmutableEncodedShort immutable) return immutable;
        return new ImmutableEncodedShort(other.getValue());
    }

    @Override
    public short getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Short.hashCode(getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof EncodedShort other
                && getValue() == other.getValue();
    }

    @Override
    public int compareTo(EncodedValue other) {
        if (other == this) return 0;
        int out = ValueType.compare(getValueType(), other.getValueType());
        if (out != 0) return out;
        return Short.compare(getValue(), ((EncodedShort) other).getValue());
    }
}
