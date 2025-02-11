package com.v7878.dex.immutable.value;

import com.v7878.dex.ValueType;

public final class EncodedShort extends EncodedValue {
    private final short value;

    private EncodedShort(short value) {
        this.value = value;
    }

    public static EncodedShort of(short value) {
        // TODO: cache values
        return new EncodedShort(value);
    }

    @Override
    public ValueType getValueType() {
        return ValueType.SHORT;
    }

    @Override
    public boolean isDefault() {
        return value == 0;
    }

    public short getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Short.hashCode(getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof EncodedShort other
                && getValue() == other.getValue();
    }

    @Override
    public int compareTo(EncodedValue other) {
        if (other == this) return 0;
        int out = ValueType.compare(getValueType(), other.getValueType());
        if (out != 0) return out;
        return Short.compare(getValue(), ((EncodedShort) other).getValue());
    }
}
