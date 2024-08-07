package com.v7878.dex.iface.value;

import com.v7878.dex.ValueType;
import com.v7878.dex.iface.FieldId;

public interface EncodedEnum extends EncodedValue {
    FieldId getValue();

    @Override
    default ValueType getValueType() {
        return ValueType.ENUM;
    }
}
