package com.v7878.dex.immutable.value;

import com.v7878.dex.ValueType;

public final class EncodedChar extends EncodedValue {
    private final char value;

    private EncodedChar(char value) {
        this.value = value;
    }

    public static EncodedChar of(char value) {
        // TODO: cache values
        return new EncodedChar(value);
    }

    @Override
    public ValueType getValueType() {
        return ValueType.CHAR;
    }

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
