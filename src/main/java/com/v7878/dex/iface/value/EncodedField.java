package com.v7878.dex.iface.value;

import com.v7878.dex.ValueType;
import com.v7878.dex.iface.FieldId;

public non-sealed interface EncodedField extends EncodedValue {
    FieldId getValue();

    @Override
    default ValueType getValueType() {
        return ValueType.FIELD;
    }
}
