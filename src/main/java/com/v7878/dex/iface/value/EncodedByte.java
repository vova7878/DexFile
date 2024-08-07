package com.v7878.dex.iface.value;

import com.v7878.dex.ValueType;

public interface EncodedByte extends EncodedValue {
    byte getValue();

    @Override
    default ValueType getValueType() {
        return ValueType.BYTE;
    }
}
