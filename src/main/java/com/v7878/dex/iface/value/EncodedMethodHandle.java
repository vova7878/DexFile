package com.v7878.dex.iface.value;

import com.v7878.dex.ValueType;
import com.v7878.dex.iface.MethodHandleId;

public non-sealed interface EncodedMethodHandle extends EncodedValue {
    MethodHandleId getValue();

    @Override
    default ValueType getValueType() {
        return ValueType.METHOD_HANDLE;
    }
}
