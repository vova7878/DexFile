package com.v7878.dex.immutable.value;

import com.v7878.dex.ValueType;

import java.util.Objects;

public class EncodedNull extends EncodedValue {
    public static final EncodedNull INSTANCE = new EncodedNull();

    protected EncodedNull() {
    }

    public static EncodedNull of(EncodedNull other) {
        Objects.requireNonNull(other);
        return INSTANCE;
    }

    @Override
    public ValueType getValueType() {
        return ValueType.NULL;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof EncodedNull;
    }

    @Override
    public int compareTo(EncodedValue other) {
        if (other == this) return 0;
        return ValueType.compare(getValueType(), other.getValueType());
    }
}
