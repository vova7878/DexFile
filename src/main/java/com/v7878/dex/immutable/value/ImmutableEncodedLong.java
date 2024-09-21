package com.v7878.dex.immutable.value;

import com.v7878.dex.ValueType;
import com.v7878.dex.base.value.BaseEncodedLong;
import com.v7878.dex.iface.value.EncodedLong;
import com.v7878.dex.iface.value.EncodedValue;

public class ImmutableEncodedLong extends BaseEncodedLong implements ImmutableEncodedValue {
    private final long value;

    protected ImmutableEncodedLong(long value) {
        this.value = value;
    }

    public static ImmutableEncodedLong of(long value) {
        return new ImmutableEncodedLong(value);
    }

    public static ImmutableEncodedLong of(EncodedLong other) {
        if (other instanceof ImmutableEncodedLong immutable) return immutable;
        return new ImmutableEncodedLong(other.getValue());
    }

    @Override
    public long getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof EncodedLong other
                && getValue() == other.getValue();
    }

    @Override
    public int compareTo(EncodedValue other) {
        if (other == this) return 0;
        int out = ValueType.compare(getValueType(), other.getValueType());
        if (out != 0) return out;
        return Long.compare(getValue(), ((EncodedLong) other).getValue());
    }
}
