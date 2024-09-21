package com.v7878.dex.immutable.value;

import com.v7878.dex.ValueType;
import com.v7878.dex.base.value.BaseEncodedDouble;
import com.v7878.dex.iface.value.EncodedDouble;
import com.v7878.dex.iface.value.EncodedValue;

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

    @Override
    public int hashCode() {
        return Double.hashCode(getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof EncodedDouble other
                && getValue() == other.getValue();
    }

    @Override
    public int compareTo(EncodedValue other) {
        if (other == this) return 0;
        int out = ValueType.compare(getValueType(), other.getValueType());
        if (out != 0) return out;
        return Double.compare(getValue(), ((EncodedDouble) other).getValue());
    }
}
