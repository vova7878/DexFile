package com.v7878.dex.immutable.value;

import com.v7878.dex.ValueType;

public final class EncodedDouble extends EncodedValue {
    private final double value;

    private EncodedDouble(double value) {
        this.value = value;
    }

    public static EncodedDouble of(double value) {
        // TODO: cache values
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
