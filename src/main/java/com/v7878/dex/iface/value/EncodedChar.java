package com.v7878.dex.iface.value;

import com.v7878.dex.ValueType;

public interface EncodedChar extends EncodedValue {
    char getValue();

    @Override
    default ValueType getValueType() {
        return ValueType.CHAR;
    }
}
