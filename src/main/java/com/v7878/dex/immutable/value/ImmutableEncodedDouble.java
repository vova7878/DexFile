package com.v7878.dex.immutable.value;

import com.v7878.dex.base.value.BaseEncodedDouble;
import com.v7878.dex.iface.value.EncodedDouble;

public class ImmutableEncodedDouble extends BaseEncodedDouble implements ImmutableEncodedValue {
    private final double value;

    protected ImmutableEncodedDouble(double value) {
        this.value = value;
    }

    public static ImmutableEncodedDouble of(double value) {
        return new ImmutableEncodedDouble(value);
    }

    public static ImmutableEncodedDouble of(EncodedDouble other) {
        if (other instanceof ImmutableEncodedDouble immutable) return immutable;
        return new ImmutableEncodedDouble(other.getValue());
    }

    @Override
    public double getValue() {
        return value;
    }
}
