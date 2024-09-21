package com.v7878.dex.immutable.value;

import com.v7878.dex.ValueType;
import com.v7878.dex.base.value.BaseEncodedArray;
import com.v7878.dex.iface.value.EncodedArray;
import com.v7878.dex.iface.value.EncodedValue;
import com.v7878.dex.util.CollectionUtils;
import com.v7878.dex.util.ItemConverter;

import java.util.List;
import java.util.Objects;

public class ImmutableEncodedArray extends BaseEncodedArray implements ImmutableEncodedValue {
    private final List<? extends ImmutableEncodedValue> value;

    protected ImmutableEncodedArray(Iterable<? extends EncodedValue> values) {
        this.value = ItemConverter.toList(ImmutableEncodedValue::of,
                value -> value instanceof ImmutableEncodedValue, values);
    }

    public static ImmutableEncodedArray of(Iterable<? extends EncodedValue> values) {
        return new ImmutableEncodedArray(values);
    }

    public static ImmutableEncodedArray of(EncodedArray other) {
        if (other instanceof ImmutableEncodedArray immutable) return immutable;
        return new ImmutableEncodedArray(other.getValue());
    }

    @Override
    public List<? extends ImmutableEncodedValue> getValue() {
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
