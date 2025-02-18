package com.v7878.dex.immutable.value;

import com.v7878.dex.ValueType;

public final class EncodedDouble extends EncodedValue {
    private final double value;

    private EncodedDouble(double value) {
        this.value = value;
    }

    public static EncodedDouble of(double value) {
        return new EncodedDouble(value);
    }

    @Override
    public ValueType getValueType() {
        return ValueType.DOUBLE;
    }

    @Override
    public boolean isDefault() {
        return Double.doubleToRawLongBits(value) == 0;
    }

    public double getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(Double.doubleToRawLongBits(getValue()));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof EncodedDouble other
                && Double.doubleToRawLongBits(getValue())
                == Double.doubleToRawLongBits(other.getValue());
    }

    @Override
    public int compareTo(EncodedValue other) {
        if (other == this) return 0;
        int out = ValueType.compare(getValueType(), other.getValueType());
        if (out != 0) return out;
        return Long.compare(Double.doubleToRawLongBits(getValue()),
                Double.doubleToRawLongBits(((EncodedDouble) other).getValue()));
    }
}
