package com.v7878.dex.immutable.value;

import com.v7878.dex.ValueType;
import com.v7878.dex.iface.CommonAnnotation;
import com.v7878.dex.iface.FieldId;
import com.v7878.dex.iface.MethodHandleId;
import com.v7878.dex.iface.MethodId;
import com.v7878.dex.iface.ProtoId;
import com.v7878.dex.iface.TypeId;
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
import com.v7878.dex.immutable.ImmutableTypeId;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;

public interface ImmutableEncodedValue extends EncodedValue {
    static ImmutableEncodedValue of(EncodedValue other) {
        return of(other.getValueType(), other);
    }

    static ImmutableEncodedValue of(ValueType type, EncodedValue other) {
        if (other instanceof ImmutableEncodedValue immutable) return immutable;
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

    static ImmutableEncodedValue defaultValue(TypeId type) {
        return switch (type.getShorty()) {
            case 'Z' -> ImmutableEncodedBoolean.of(false);
            case 'B' -> ImmutableEncodedByte.of((byte) 0);
            case 'S' -> ImmutableEncodedShort.of((short) 0);
            case 'C' -> ImmutableEncodedChar.of((char) 0);
            case 'I' -> ImmutableEncodedInt.of(0);
            case 'J' -> ImmutableEncodedLong.of(0);
            case 'F' -> ImmutableEncodedFloat.of(0);
            case 'D' -> ImmutableEncodedDouble.of(0);
            case 'L' -> ImmutableEncodedNull.INSTANCE;
            // V - void
            default -> throw new IllegalArgumentException("Unexpected shorty");
        };
    }

    static ImmutableEncodedValue of(Object obj) {
        if (obj == null) return ImmutableEncodedNull.INSTANCE;
        if (obj instanceof EncodedValue value) return of(value);
        if (obj instanceof Boolean value) ImmutableEncodedBoolean.of(value);
        if (obj instanceof Byte value) return ImmutableEncodedByte.of(value);
        if (obj instanceof Short value) return ImmutableEncodedShort.of(value);
        if (obj instanceof Character value) return ImmutableEncodedChar.of(value);
        if (obj instanceof Integer value) return ImmutableEncodedInt.of(value);
        if (obj instanceof Float value) return ImmutableEncodedFloat.of(value);
        if (obj instanceof Long value) return ImmutableEncodedLong.of(value);
        if (obj instanceof Double value) return ImmutableEncodedDouble.of(value);
        if (obj instanceof String value) return ImmutableEncodedString.of(value);
        if (obj instanceof TypeId value) return ImmutableEncodedType.of(value);
        if (obj instanceof ProtoId value) return ImmutableEncodedMethodType.of(value);
        if (obj instanceof MethodId value) return ImmutableEncodedMethod.of(value);
        if (obj instanceof FieldId value) return ImmutableEncodedField.of(value);
        if (obj instanceof MethodHandleId value) return ImmutableEncodedMethodHandle.of(value);
        if (obj instanceof CommonAnnotation value)
            return ImmutableEncodedAnnotation.of(value.getType(), value.getElements());

        if (obj instanceof Class<?> value)
            return ImmutableEncodedType.of(ImmutableTypeId.of(value));
        if (obj instanceof MethodType) {
            //TODO
            throw new UnsupportedOperationException("not implemented yet");
        }
        if (obj instanceof Executable) {
            //TODO
            throw new UnsupportedOperationException("not implemented yet");
        }
        if (obj instanceof Field) {
            //TODO
            throw new UnsupportedOperationException("not implemented yet");
        }
        if (obj instanceof Enum<?>) {
            //TODO
            throw new UnsupportedOperationException("not implemented yet");
        }
        if (obj instanceof MethodHandle) {
            //TODO
            throw new UnsupportedOperationException("not implemented yet");
        }
        if (obj.getClass().isAnnotation()) {
            //TODO
            throw new UnsupportedOperationException("not implemented yet");
        }
        if (obj instanceof boolean[]) {
            //TODO
            throw new UnsupportedOperationException("not implemented yet");
        }
        if (obj instanceof byte[]) {
            //TODO
            throw new UnsupportedOperationException("not implemented yet");
        }
        if (obj instanceof short[]) {
            //TODO
            throw new UnsupportedOperationException("not implemented yet");
        }
        if (obj instanceof char[]) {
            //TODO
            throw new UnsupportedOperationException("not implemented yet");
        }
        if (obj instanceof int[]) {
            //TODO
            throw new UnsupportedOperationException("not implemented yet");
        }
        if (obj instanceof float[]) {
            //TODO
            throw new UnsupportedOperationException("not implemented yet");
        }
        if (obj instanceof long[]) {
            //TODO
            throw new UnsupportedOperationException("not implemented yet");
        }
        if (obj instanceof double[]) {
            //TODO
            throw new UnsupportedOperationException("not implemented yet");
        }
        if (obj instanceof Object[]) {
            //TODO
            throw new UnsupportedOperationException("not implemented yet");
        }
        if (obj instanceof Iterable<?>) {
            //TODO
            throw new UnsupportedOperationException("not implemented yet");
        }
        throw new IllegalArgumentException(
                "Unable to convert " + obj + " to ImmutableEncodedValue");
    }
}
