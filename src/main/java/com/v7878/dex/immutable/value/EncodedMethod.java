package com.v7878.dex.immutable.value;

import com.v7878.dex.ValueType;
import com.v7878.dex.immutable.MethodId;
import com.v7878.dex.util.CollectionUtils;

import java.util.Objects;

public class EncodedMethod extends EncodedValue {
    private final MethodId value;

    protected EncodedMethod(MethodId value) {
        this.value = Objects.requireNonNull(value);
    }

    public static EncodedMethod of(MethodId value) {
        return new EncodedMethod(value);
    }

    @Override
    public ValueType getValueType() {
        return ValueType.METHOD;
    }

    public MethodId getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof EncodedMethod other
                && Objects.equals(getValue(), other.getValue());
    }

    @Override
    public int compareTo(EncodedValue other) {
        if (other == this) return 0;
        int out = ValueType.compare(getValueType(), other.getValueType());
        if (out != 0) return out;
        return CollectionUtils.compareNonNull(getValue(), ((EncodedMethod) other).getValue());
    }
}
