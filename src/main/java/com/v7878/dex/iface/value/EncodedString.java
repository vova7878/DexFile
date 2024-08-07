package com.v7878.dex.iface.value;

import com.v7878.dex.ValueType;

public interface EncodedString extends EncodedValue {
    String getValue();

    @Override
    default ValueType getValueType() {
        return ValueType.STRING;
    }
}
