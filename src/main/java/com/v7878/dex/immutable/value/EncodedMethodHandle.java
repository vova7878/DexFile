package com.v7878.dex.immutable.value;

import com.v7878.dex.ValueType;
import com.v7878.dex.immutable.MethodHandleId;
import com.v7878.dex.util.CollectionUtils;

import java.util.Objects;

public final class EncodedMethodHandle extends EncodedValue {
    private final MethodHandleId value;

    private EncodedMethodHandle(MethodHandleId value) {
        this.value = Objects.requireNonNull(value);
    }

    public static EncodedMethodHandle of(MethodHandleId value) {
        return new EncodedMethodHandle(value);
    }

    @Override
    public ValueType getValueType() {
        return ValueType.METHOD_HANDLE;
    }

    public MethodHandleId getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof EncodedMethodHandle other
                && Objects.equals(getValue(), other.getValue());
    }

    @Override
    public int compareTo(EncodedValue other) {
        if (other == this) return 0;
        int out = ValueType.compare(getValueType(), other.getValueType());
        if (out != 0) return out;
        return CollectionUtils.compareNonNull(getValue(), ((EncodedMethodHandle) other).getValue());
    }
}
