package com.v7878.dex.iface.value;

import com.v7878.dex.ValueType;

public interface EncodedFloat extends EncodedValue {
    float getValue();

    @Override
    default ValueType getValueType() {
        return ValueType.FLOAT;
    }
}
