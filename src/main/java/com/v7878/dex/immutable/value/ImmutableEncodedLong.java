package com.v7878.dex.immutable.value;

import com.v7878.dex.base.value.BaseEncodedLong;
import com.v7878.dex.iface.value.EncodedLong;

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
}
