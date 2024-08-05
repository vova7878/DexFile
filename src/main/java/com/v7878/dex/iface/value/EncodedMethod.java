package com.v7878.dex.iface.value;

import com.v7878.dex.ValueType;
import com.v7878.dex.iface.MethodId;

public non-sealed interface EncodedMethod extends EncodedValue {
    MethodId getValue();

    @Override
    default ValueType getValueType() {
        return ValueType.METHOD;
    }
}
