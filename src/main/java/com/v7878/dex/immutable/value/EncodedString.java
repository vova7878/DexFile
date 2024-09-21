package com.v7878.dex.immutable.value;

import com.v7878.dex.ValueType;
import com.v7878.dex.util.CollectionUtils;

import java.util.Objects;

public class EncodedString extends EncodedValue {
    private final String value;

    protected EncodedString(String value) {
        this.value = Objects.requireNonNull(value);
    }

    public static EncodedString of(String value) {
        return new EncodedString(value);
    }

    @Override
    public ValueType getValueType() {
        return ValueType.STRING;
    }

    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof EncodedString other
                && Objects.equals(getValue(), other.getValue());
    }

    @Override
    public int compareTo(EncodedValue other) {
        if (other == this) return 0;
        int out = ValueType.compare(getValueType(), other.getValueType());
        if (out != 0) return out;
        return CollectionUtils.compareNonNull(getValue(), ((EncodedString) other).getValue());
    }
}
