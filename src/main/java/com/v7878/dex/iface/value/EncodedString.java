package com.v7878.dex.iface.value;

import com.v7878.dex.ValueType;

public non-sealed interface EncodedString extends EncodedValue {
    String getValue();

    @Override
    default ValueType getValueType() {
        return ValueType.STRING;
    }
}
