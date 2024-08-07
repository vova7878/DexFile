package com.v7878.dex.iface.value;

import com.v7878.dex.ValueType;
import com.v7878.dex.iface.ProtoId;

public interface EncodedMethodType extends EncodedValue {
    ProtoId getValue();

    @Override
    default ValueType getValueType() {
        return ValueType.METHOD_TYPE;
    }
}
