package com.v7878.dex.immutable.value;

import com.v7878.dex.base.value.BaseEncodedShort;
import com.v7878.dex.iface.value.EncodedShort;

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
}
