package com.v7878.dex.iface.value;

import com.v7878.dex.ValueType;

public interface EncodedValue extends Comparable<EncodedValue> {
    ValueType getValueType();
}
