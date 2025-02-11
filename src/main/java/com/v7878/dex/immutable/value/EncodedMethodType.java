package com.v7878.dex.immutable.value;

import com.v7878.dex.ValueType;
import com.v7878.dex.immutable.ProtoId;
import com.v7878.dex.util.CollectionUtils;

import java.util.Objects;

public final class EncodedMethodType extends EncodedValue {
    private final ProtoId value;

    private EncodedMethodType(ProtoId value) {
        this.value = Objects.requireNonNull(value);
    }

    public static EncodedMethodType of(ProtoId value) {
        return new EncodedMethodType(value);
    }

    @Override
    public ValueType getValueType() {
        return ValueType.METHOD_TYPE;
    }

    @Override
    public boolean isDefault() {
        return false;
    }

    public ProtoId getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof EncodedMethodType other
                && Objects.equals(getValue(), other.getValue());
    }

    @Override
    public int compareTo(EncodedValue other) {
        if (other == this) return 0;
        int out = ValueType.compare(getValueType(), other.getValueType());
        if (out != 0) return out;
        return CollectionUtils.compareNonNull(getValue(), ((EncodedMethodType) other).getValue());
    }
}
