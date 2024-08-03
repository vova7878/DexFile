package com.v7878.dex.iface.value;

import com.v7878.dex.EncodedValueType;

public sealed interface EncodedValue extends Comparable<EncodedValue> permits
        EncodedAnnotation, EncodedArray, EncodedBoolean, EncodedByte,
        EncodedChar, EncodedDouble, EncodedEnum, EncodedField, EncodedFloat,
        EncodedInt, EncodedMethod, EncodedMethodHandle, EncodedMethodType,
        EncodedNull, EncodedShort, EncodedString, EncodedType {
    EncodedValueType getValueType();
}
