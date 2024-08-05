package com.v7878.dex.iface.value;

import com.v7878.dex.ValueType;

public non-sealed interface EncodedInt extends EncodedValue {
    int getValue();

    @Override
    default ValueType getValueType() {
        return ValueType.INT;
    }
}
