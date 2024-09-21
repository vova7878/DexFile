package com.v7878.dex.immutable.value;

import com.v7878.dex.ValueType;
import com.v7878.dex.base.value.BaseEncodedFloat;
import com.v7878.dex.iface.value.EncodedFloat;
import com.v7878.dex.iface.value.EncodedValue;

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

    @Override
    public int hashCode() {
        return Float.hashCode(getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof EncodedFloat other
                && getValue() == other.getValue();
    }

    @Override
    public int compareTo(EncodedValue other) {
        if (other == this) return 0;
        int out = ValueType.compare(getValueType(), other.getValueType());
        if (out != 0) return out;
        return Float.compare(getValue(), ((EncodedFloat) other).getValue());
    }
}
