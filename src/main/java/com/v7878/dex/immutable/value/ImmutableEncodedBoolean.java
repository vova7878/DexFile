package com.v7878.dex.immutable.value;

import com.v7878.dex.base.value.BaseEncodedBoolean;
import com.v7878.dex.iface.value.EncodedBoolean;

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
}
