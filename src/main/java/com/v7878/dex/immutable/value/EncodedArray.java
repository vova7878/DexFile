package com.v7878.dex.immutable.value;

import com.v7878.dex.ValueType;
import com.v7878.dex.util.CollectionUtils;
import com.v7878.dex.util.ItemConverter;

import java.util.List;
import java.util.Objects;

public final class EncodedArray extends EncodedValue {
    private final List<EncodedValue> value;

    private EncodedArray(Iterable<EncodedValue> values) {
        this.value = ItemConverter.toList(values);
    }

    public static EncodedArray of(Iterable<EncodedValue> values) {
        return new EncodedArray(values);
    }

    @Override
    public ValueType getValueType() {
        return ValueType.ARRAY;
    }

    public List<EncodedValue> getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof EncodedArray other
                && Objects.equals(getValue(), other.getValue());
    }

    @Override
    public int compareTo(EncodedValue other) {
        if (other == this) return 0;
        int out = ValueType.compare(getValueType(), other.getValueType());
        if (out != 0) return out;
        return CollectionUtils.compareLexicographically(
                getValue(), ((EncodedArray) other).getValue());
    }
}
