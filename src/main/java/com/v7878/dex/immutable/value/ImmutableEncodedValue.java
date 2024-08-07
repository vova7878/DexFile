package com.v7878.dex.immutable.value;

import com.v7878.dex.ValueType;
import com.v7878.dex.iface.value.EncodedValue;

public interface ImmutableEncodedValue extends EncodedValue {
    static ImmutableEncodedValue of(EncodedValue other) {
        throw new UnsupportedOperationException("TODO");
    }

    static ImmutableEncodedValue of(EncodedValue other, ValueType type) {
        throw new UnsupportedOperationException("TODO");
    }
}
