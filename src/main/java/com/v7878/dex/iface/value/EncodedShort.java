package com.v7878.dex.iface.value;

import com.v7878.dex.ValueType;

public interface EncodedShort extends EncodedValue {
    short getValue();

    @Override
    default ValueType getValueType() {
        return ValueType.SHORT;
    }
}
