package com.v7878.dex.immutable.value;

import com.v7878.dex.ValueType;
import com.v7878.dex.base.value.BaseEncodedChar;
import com.v7878.dex.iface.value.EncodedChar;
import com.v7878.dex.iface.value.EncodedValue;

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

    @Override
    public int hashCode() {
        return Character.hashCode(getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof EncodedChar other
                && getValue() == other.getValue();
    }

    @Override
    public int compareTo(EncodedValue other) {
        if (other == this) return 0;
        int out = ValueType.compare(getValueType(), other.getValueType());
        if (out != 0) return out;
        return Character.compare(getValue(), ((EncodedChar) other).getValue());
    }
}
