package com.v7878.dex.immutable.value;

import com.v7878.dex.base.value.BaseEncodedInt;
import com.v7878.dex.iface.value.EncodedInt;

public class ImmutableEncodedInt extends BaseEncodedInt implements ImmutableEncodedValue {
    private final int value;

    protected ImmutableEncodedInt(int value) {
        this.value = value;
    }

    public static ImmutableEncodedInt of(int value) {
        return new ImmutableEncodedInt(value);
    }

    public static ImmutableEncodedInt of(EncodedInt other) {
        if (other instanceof ImmutableEncodedInt immutable) return immutable;
        return new ImmutableEncodedInt(other.getValue());
    }

    @Override
    public int getValue() {
        return value;
    }
}
