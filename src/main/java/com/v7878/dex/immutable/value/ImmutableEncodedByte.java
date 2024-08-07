package com.v7878.dex.immutable.value;

import com.v7878.dex.base.value.BaseEncodedByte;
import com.v7878.dex.iface.value.EncodedByte;

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
}
