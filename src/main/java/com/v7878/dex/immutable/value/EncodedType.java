package com.v7878.dex.immutable.value;

import com.v7878.dex.ValueType;
import com.v7878.dex.immutable.TypeId;
import com.v7878.dex.util.CollectionUtils;

import java.util.Objects;

public final class EncodedType extends EncodedValue {
    private final TypeId value;

    private EncodedType(TypeId value) {
        this.value = Objects.requireNonNull(value);
    }

    public static EncodedType of(TypeId value) {
        return new EncodedType(value);
    }

    @Override
    public ValueType getValueType() {
        return ValueType.TYPE;
    }

    public TypeId getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof EncodedType other
                && Objects.equals(getValue(), other.getValue());
    }

    @Override
    public int compareTo(EncodedValue other) {
        if (other == this) return 0;
        int out = ValueType.compare(getValueType(), other.getValueType());
        if (out != 0) return out;
        return CollectionUtils.compareNonNull(getValue(), ((EncodedType) other).getValue());
    }
}
