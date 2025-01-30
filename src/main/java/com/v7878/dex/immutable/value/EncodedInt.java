package com.v7878.dex.immutable.value;

import com.v7878.dex.ValueType;

public final class EncodedInt extends EncodedValue {
    private final int value;

    private EncodedInt(int value) {
        this.value = value;
    }

    public static EncodedInt of(int value) {
        // TODO: cache values
        return new EncodedInt(value);
    }

    @Override
    public ValueType getValueType() {
        return ValueType.INT;
    }

    public int getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof EncodedInt other
                && getValue() == other.getValue();
    }

    @Override
    public int compareTo(EncodedValue other) {
        if (other == this) return 0;
        int out = ValueType.compare(getValueType(), other.getValueType());
        if (out != 0) return out;
        return Integer.compare(getValue(), ((EncodedInt) other).getValue());
    }
}
