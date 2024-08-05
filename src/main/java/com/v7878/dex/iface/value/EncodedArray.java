package com.v7878.dex.iface.value;

import com.v7878.dex.ValueType;

import java.util.List;

public non-sealed interface EncodedArray extends EncodedValue {
    List<? extends EncodedValue> getValue();

    @Override
    default ValueType getValueType() {
        return ValueType.ARRAY;
    }
}
