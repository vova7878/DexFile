package com.v7878.dex.iface.value;

import com.v7878.dex.ValueType;

public non-sealed interface EncodedBoolean extends EncodedValue {
    boolean getValue();

    @Override
    default ValueType getValueType() {
        return ValueType.BOOLEAN;
    }
}
