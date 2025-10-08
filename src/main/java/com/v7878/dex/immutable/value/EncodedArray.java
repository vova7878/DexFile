package com.v7878.dex.immutable.value;

import com.v7878.dex.Internal;
import com.v7878.dex.ValueType;
import com.v7878.dex.util.CollectionUtils;
import com.v7878.dex.util.Converter;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class EncodedArray extends EncodedValue {
    public static final EncodedArray EMPTY = new EncodedArray(Collections.emptyList());

    private final List<EncodedValue> value;

    private EncodedArray(List<EncodedValue> value) {
        this.value = Objects.requireNonNull(value);
    }

    @Internal
    public static EncodedArray raw(List<EncodedValue> values) {
        if (values.isEmpty()) return EMPTY;
        return new EncodedArray(values);
    }

    public static EncodedArray of(Iterable<EncodedValue> values) {
        var value = Converter.toList(values);
        if (value.isEmpty()) return EMPTY;
        return new EncodedArray(value);
    }

    @Override
    public ValueType getValueType() {
        return ValueType.ARRAY;
    }

    @Override
    public boolean isDefault() {
        return false;
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
