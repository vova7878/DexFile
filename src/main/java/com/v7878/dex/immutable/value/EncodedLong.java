package com.v7878.dex.immutable.value;

import com.v7878.dex.ValueType;

public final class EncodedLong extends EncodedValue {
    private final long value;

    private EncodedLong(long value) {
        this.value = value;
    }

    public static EncodedLong of(long value) {
        // TODO: cache values
        return new EncodedLong(value);
    }

    @Override
    public ValueType getValueType() {
        return ValueType.LONG;
    }

    @Override
    public boolean isDefault() {
        return value == 0;
    }

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
