package com.v7878.dex.immutable.value;

import com.v7878.dex.ValueType;
import com.v7878.dex.base.value.BaseEncodedByte;
import com.v7878.dex.iface.value.EncodedByte;
import com.v7878.dex.iface.value.EncodedValue;

public class ImmutableEncodedByte extends BaseEncodedByte implements ImmutableEncodedValue {
    private final byte value;

    protected ImmutableEncodedByte(byte value) {
        this.value = value;
    }

    public static ImmutableEncodedByte of(byte value) {
        return new ImmutableEncodedByte(value);
    }

    public static ImmutableEncodedByte of(EncodedByte other) {
        if (other instanceof ImmutableEncodedByte immutable) return immutable;
        return new ImmutableEncodedByte(other.getValue());
    }

    @Override
    public byte getValue() {
        return value;
    }

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
