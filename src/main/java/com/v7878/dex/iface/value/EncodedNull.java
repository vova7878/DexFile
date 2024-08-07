package com.v7878.dex.iface.value;

import com.v7878.dex.ValueType;

public interface EncodedNull extends EncodedValue {
    @Override
    default ValueType getValueType() {
        return ValueType.NULL;
    }
}
