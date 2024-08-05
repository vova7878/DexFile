package com.v7878.dex.iface.value;

import com.v7878.dex.ValueType;
import com.v7878.dex.iface.CommonAnnotation;

public non-sealed interface EncodedAnnotation extends EncodedValue, CommonAnnotation {
    @Override
    default ValueType getValueType() {
        return ValueType.ANNOTATION;
    }
}
