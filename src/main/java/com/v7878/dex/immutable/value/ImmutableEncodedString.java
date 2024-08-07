package com.v7878.dex.immutable.value;

import com.v7878.dex.base.value.BaseEncodedString;
import com.v7878.dex.iface.value.EncodedString;

import java.util.Objects;

public class ImmutableEncodedString extends BaseEncodedString implements ImmutableEncodedValue {
    private final String value;

    protected ImmutableEncodedString(String value) {
        this.value = Objects.requireNonNull(value);
    }

    public static ImmutableEncodedString of(String value) {
        return new ImmutableEncodedString(value);
    }

    public static ImmutableEncodedString of(EncodedString other) {
        if (other instanceof ImmutableEncodedString immutable) return immutable;
        return new ImmutableEncodedString(other.getValue());
    }

    @Override
    public String getValue() {
        return value;
    }
}
