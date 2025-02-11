package com.v7878.dex.immutable.value;

import com.v7878.dex.ValueType;
import com.v7878.dex.immutable.FieldId;
import com.v7878.dex.util.CollectionUtils;

import java.util.Objects;

public final class EncodedEnum extends EncodedValue {
    private final FieldId value;

    private EncodedEnum(FieldId value) {
        this.value = Objects.requireNonNull(value);
    }

    public static EncodedEnum of(FieldId value) {
        return new EncodedEnum(value);
    }

    @Override
    public ValueType getValueType() {
        return ValueType.ENUM;
    }

    @Override
    public boolean isDefault() {
        return false;
    }

    public FieldId getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof EncodedEnum other
                && Objects.equals(getValue(), other.getValue());
    }

    @Override
    public int compareTo(EncodedValue other) {
        if (other == this) return 0;
        int out = ValueType.compare(getValueType(), other.getValueType());
        if (out != 0) return out;
        return CollectionUtils.compareNonNull(getValue(), ((EncodedEnum) other).getValue());
    }
}
