package com.v7878.dex.immutable.value;

import com.v7878.dex.ValueType;
import com.v7878.dex.base.value.BaseEncodedBoolean;
import com.v7878.dex.iface.value.EncodedBoolean;
import com.v7878.dex.iface.value.EncodedValue;

public class ImmutableEncodedBoolean extends BaseEncodedBoolean implements ImmutableEncodedValue {
    private final boolean value;

    protected ImmutableEncodedBoolean(boolean value) {
        this.value = value;
    }

    public static ImmutableEncodedBoolean of(boolean value) {
        return new ImmutableEncodedBoolean(value);
    }

    public static ImmutableEncodedBoolean of(EncodedBoolean other) {
        if (other instanceof ImmutableEncodedBoolean immutable) return immutable;
        return new ImmutableEncodedBoolean(other.getValue());
    }

    @Override
    public boolean getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof EncodedBoolean other
                && getValue() == other.getValue();
    }

    @Override
    public int compareTo(EncodedValue other) {
        if (other == this) return 0;
        int out = ValueType.compare(getValueType(), other.getValueType());
        if (out != 0) return out;
        return Boolean.compare(getValue(), ((EncodedBoolean) other).getValue());
    }
}
