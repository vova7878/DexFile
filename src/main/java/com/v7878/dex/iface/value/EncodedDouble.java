package com.v7878.dex.iface.value;

import com.v7878.dex.ValueType;

public interface EncodedDouble extends EncodedValue {
    double getValue();

    @Override
    default ValueType getValueType() {
        return ValueType.DOUBLE;
    }
}
