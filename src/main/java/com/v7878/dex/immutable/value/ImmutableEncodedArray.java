package com.v7878.dex.immutable.value;

import com.v7878.dex.base.value.BaseEncodedArray;
import com.v7878.dex.iface.value.EncodedArray;
import com.v7878.dex.iface.value.EncodedValue;
import com.v7878.dex.util.ItemConverter;

import java.util.List;

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
}
