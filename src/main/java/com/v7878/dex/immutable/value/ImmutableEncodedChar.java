package com.v7878.dex.immutable.value;

import com.v7878.dex.base.value.BaseEncodedChar;
import com.v7878.dex.iface.value.EncodedChar;

public class ImmutableEncodedChar extends BaseEncodedChar implements ImmutableEncodedValue {
    private final char value;

    protected ImmutableEncodedChar(char value) {
        this.value = value;
    }

    public static ImmutableEncodedChar of(char value) {
        return new ImmutableEncodedChar(value);
    }

    public static ImmutableEncodedChar of(EncodedChar other) {
        if (other instanceof ImmutableEncodedChar immutable) return immutable;
        return new ImmutableEncodedChar(other.getValue());
    }

    @Override
    public char getValue() {
        return value;
    }
}
