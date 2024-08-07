package com.v7878.dex.immutable.value;

import com.v7878.dex.base.value.BaseEncodedFloat;
import com.v7878.dex.iface.value.EncodedFloat;

public class ImmutableEncodedFloat extends BaseEncodedFloat implements ImmutableEncodedValue {
    private final float value;

    protected ImmutableEncodedFloat(float value) {
        this.value = value;
    }

    public static ImmutableEncodedFloat of(float value) {
        return new ImmutableEncodedFloat(value);
    }

    public static ImmutableEncodedFloat of(EncodedFloat other) {
        if (other instanceof ImmutableEncodedFloat immutable) return immutable;
        return new ImmutableEncodedFloat(other.getValue());
    }

    @Override
    public float getValue() {
        return value;
    }
}
