package com.v7878.dex.immutable.value;

import com.v7878.dex.ValueType;
import com.v7878.dex.immutable.FieldId;
import com.v7878.dex.util.CollectionUtils;

import java.util.Objects;

public class EncodedField extends EncodedValue {
    private final FieldId value;

    protected EncodedField(FieldId value) {
        this.value = Objects.requireNonNull(value);
    }

    public static EncodedField of(FieldId value) {
        return new EncodedField(value);
    }

    @Override
    public ValueType getValueType() {
        return ValueType.FIELD;
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
        return obj instanceof EncodedField other
                && Objects.equals(getValue(), other.getValue());
    }

    @Override
    public int compareTo(EncodedValue other) {
        if (other == this) return 0;
        int out = ValueType.compare(getValueType(), other.getValueType());
        if (out != 0) return out;
        return CollectionUtils.compareNonNull(getValue(), ((EncodedField) other).getValue());
    }
}
