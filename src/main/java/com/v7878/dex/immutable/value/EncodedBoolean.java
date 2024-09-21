package com.v7878.dex.immutable.value;

import com.v7878.dex.ValueType;

public final class EncodedBoolean extends EncodedValue {
    private final boolean value;

    private EncodedBoolean(boolean value) {
        this.value = value;
    }

    public static EncodedBoolean of(boolean value) {
        return new EncodedBoolean(value);
    }

    @Override
    public ValueType getValueType() {
        return ValueType.BOOLEAN;
    }

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
