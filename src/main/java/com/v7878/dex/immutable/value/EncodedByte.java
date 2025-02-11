package com.v7878.dex.immutable.value;

import com.v7878.dex.ValueType;

public final class EncodedByte extends EncodedValue {
    private final byte value;

    private EncodedByte(byte value) {
        this.value = value;
    }

    public static EncodedByte of(byte value) {
        // TODO: cache values
        return new EncodedByte(value);
    }

    @Override
    public ValueType getValueType() {
        return ValueType.BYTE;
    }

    @Override
    public boolean isDefault() {
        return value == 0;
    }

    public byte getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Byte.hashCode(getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof EncodedByte other
                && getValue() == other.getValue();
    }

    @Override
    public int compareTo(EncodedValue other) {
        if (other == this) return 0;
        int out = ValueType.compare(getValueType(), other.getValueType());
        if (out != 0) return out;
        return Byte.compare(getValue(), ((EncodedByte) other).getValue());
    }
}
