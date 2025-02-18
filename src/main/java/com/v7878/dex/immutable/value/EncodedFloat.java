package com.v7878.dex.immutable.value;

import com.v7878.dex.ValueType;

public final class EncodedFloat extends EncodedValue {
    private final float value;

    private EncodedFloat(float value) {
        this.value = value;
    }

    public static EncodedFloat of(float value) {
        return new EncodedFloat(value);
    }

    @Override
    public ValueType getValueType() {
        return ValueType.FLOAT;
    }

    @Override
    public boolean isDefault() {
        return Float.floatToRawIntBits(value) == 0;
    }

    public float getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(Float.floatToRawIntBits(getValue()));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof EncodedFloat other
                && Float.floatToRawIntBits(getValue())
                == Float.floatToRawIntBits(other.getValue());
    }

    @Override
    public int compareTo(EncodedValue other) {
        if (other == this) return 0;
        int out = ValueType.compare(getValueType(), other.getValueType());
        if (out != 0) return out;
        return Integer.compare(Float.floatToRawIntBits(getValue()),
                Float.floatToRawIntBits(((EncodedFloat) other).getValue()));
    }
}
