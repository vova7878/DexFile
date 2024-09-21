package com.v7878.dex.immutable.value;

import com.v7878.dex.ValueType;

public class EncodedDouble extends EncodedValue {
    private final double value;

    protected EncodedDouble(double value) {
        this.value = value;
    }

    public static EncodedDouble of(double value) {
        return new EncodedDouble(value);
    }

    @Override
    public ValueType getValueType() {
        return ValueType.DOUBLE;
    }

    public double getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof EncodedDouble other
                && getValue() == other.getValue();
    }

    @Override
    public int compareTo(EncodedValue other) {
        if (other == this) return 0;
        int out = ValueType.compare(getValueType(), other.getValueType());
        if (out != 0) return out;
        return Double.compare(getValue(), ((EncodedDouble) other).getValue());
    }
}
