package com.v7878.dex.immutable.value;

import com.v7878.dex.ValueType;

public final class EncodedFloat extends EncodedValue {
    private final float value;

    private EncodedFloat(float value) {
        this.value = value;
    }

    public static EncodedFloat of(float value) {
        // TODO: cache values
        return new EncodedFloat(value);
    }

    @Override
    public ValueType getValueType() {
        return ValueType.FLOAT;
    }

    public float getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Float.hashCode(getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof EncodedFloat other
                && getValue() == other.getValue();
    }

    @Override
    public int compareTo(EncodedValue other) {
        if (other == this) return 0;
        int out = ValueType.compare(getValueType(), other.getValueType());
        if (out != 0) return out;
        return Float.compare(getValue(), ((EncodedFloat) other).getValue());
    }
}
