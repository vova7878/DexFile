package com.v7878.dex.iface.value;

import com.v7878.dex.ValueType;
import com.v7878.dex.iface.TypeId;

public non-sealed interface EncodedType extends EncodedValue {
    TypeId getValue();

    @Override
    default ValueType getValueType() {
        return ValueType.TYPE;
    }
}
