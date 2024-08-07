package com.v7878.dex.iface.value;

import com.v7878.dex.ValueType;

public interface EncodedLong extends EncodedValue {
    long getValue();

    @Override
    default ValueType getValueType() {
        return ValueType.LONG;
    }
}
