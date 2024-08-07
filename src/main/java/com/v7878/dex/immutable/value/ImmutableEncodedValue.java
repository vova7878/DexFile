package com.v7878.dex.immutable.value;

import com.v7878.dex.ValueType;
import com.v7878.dex.iface.value.EncodedAnnotation;
import com.v7878.dex.iface.value.EncodedArray;
import com.v7878.dex.iface.value.EncodedBoolean;
import com.v7878.dex.iface.value.EncodedByte;
import com.v7878.dex.iface.value.EncodedChar;
import com.v7878.dex.iface.value.EncodedDouble;
import com.v7878.dex.iface.value.EncodedEnum;
import com.v7878.dex.iface.value.EncodedField;
import com.v7878.dex.iface.value.EncodedFloat;
import com.v7878.dex.iface.value.EncodedInt;
import com.v7878.dex.iface.value.EncodedLong;
import com.v7878.dex.iface.value.EncodedMethod;
import com.v7878.dex.iface.value.EncodedMethodHandle;
import com.v7878.dex.iface.value.EncodedMethodType;
import com.v7878.dex.iface.value.EncodedNull;
import com.v7878.dex.iface.value.EncodedShort;
import com.v7878.dex.iface.value.EncodedString;
import com.v7878.dex.iface.value.EncodedType;
import com.v7878.dex.iface.value.EncodedValue;

public interface ImmutableEncodedValue extends EncodedValue {
    static ImmutableEncodedValue of(EncodedValue other) {
        return of(other.getValueType(), other);
    }

    static ImmutableEncodedValue of(ValueType type, EncodedValue other) {
        return switch (type) {
            case BYTE -> ImmutableEncodedByte.of((EncodedByte) other);
            case BOOLEAN -> ImmutableEncodedBoolean.of((EncodedBoolean) other);
            case SHORT -> ImmutableEncodedShort.of((EncodedShort) other);
            case CHAR -> ImmutableEncodedChar.of((EncodedChar) other);
            case INT -> ImmutableEncodedInt.of((EncodedInt) other);
            case FLOAT -> ImmutableEncodedFloat.of((EncodedFloat) other);
            case LONG -> ImmutableEncodedLong.of((EncodedLong) other);
            case DOUBLE -> ImmutableEncodedDouble.of((EncodedDouble) other);
            case STRING -> ImmutableEncodedString.of((EncodedString) other);
            case TYPE -> ImmutableEncodedType.of((EncodedType) other);
            case FIELD -> ImmutableEncodedField.of((EncodedField) other);
            case ENUM -> ImmutableEncodedEnum.of((EncodedEnum) other);
            case METHOD -> ImmutableEncodedMethod.of((EncodedMethod) other);
            case METHOD_TYPE -> ImmutableEncodedMethodType.of((EncodedMethodType) other);
            case METHOD_HANDLE -> ImmutableEncodedMethodHandle.of((EncodedMethodHandle) other);
            case ARRAY -> ImmutableEncodedArray.of((EncodedArray) other);
            case ANNOTATION -> ImmutableEncodedAnnotation.of((EncodedAnnotation) other);
            case NULL -> ImmutableEncodedNull.of((EncodedNull) other);
        };
    }
}
