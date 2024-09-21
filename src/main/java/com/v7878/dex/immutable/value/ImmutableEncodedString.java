package com.v7878.dex.immutable.value;

import com.v7878.dex.ValueType;
import com.v7878.dex.base.value.BaseEncodedString;
import com.v7878.dex.iface.value.EncodedString;
import com.v7878.dex.iface.value.EncodedValue;
import com.v7878.dex.util.CollectionUtils;

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

    @Override
    public int hashCode() {
        return Objects.hashCode(getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof EncodedString other
                && Objects.equals(getValue(), other.getValue());
    }

    @Override
    public int compareTo(EncodedValue other) {
        if (other == this) return 0;
        int out = ValueType.compare(getValueType(), other.getValueType());
        if (out != 0) return out;
        return CollectionUtils.compareNonNull(getValue(), ((EncodedString) other).getValue());
    }
}
