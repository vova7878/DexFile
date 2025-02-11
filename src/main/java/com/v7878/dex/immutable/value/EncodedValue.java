package com.v7878.dex.immutable.value;

import com.v7878.dex.ValueType;
import com.v7878.dex.immutable.CommonAnnotation;
import com.v7878.dex.immutable.FieldId;
import com.v7878.dex.immutable.MethodHandleId;
import com.v7878.dex.immutable.MethodId;
import com.v7878.dex.immutable.ProtoId;
import com.v7878.dex.immutable.TypeId;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;

public abstract sealed class EncodedValue implements Comparable<EncodedValue>
        permits EncodedAnnotation, EncodedArray, EncodedBoolean, EncodedByte,
        EncodedChar, EncodedDouble, EncodedEnum, EncodedField, EncodedFloat,
        EncodedInt, EncodedLong, EncodedMethod, EncodedMethodHandle, EncodedMethodType,
        EncodedNull, EncodedShort, EncodedString, EncodedType {
    public abstract ValueType getValueType();

    public abstract boolean isDefault();

    public static EncodedValue defaultValue(TypeId type) {
        return switch (type.getShorty()) {
            case 'Z' -> EncodedBoolean.of(false);
            case 'B' -> EncodedByte.of((byte) 0);
            case 'S' -> EncodedShort.of((short) 0);
            case 'C' -> EncodedChar.of((char) 0);
            case 'I' -> EncodedInt.of(0);
            case 'J' -> EncodedLong.of(0);
            case 'F' -> EncodedFloat.of(0);
            case 'D' -> EncodedDouble.of(0);
            case 'L' -> EncodedNull.INSTANCE;
            // V - void
            default -> throw new IllegalArgumentException("Unexpected shorty");
        };
    }

    public static EncodedValue ofValue(Object obj) {
        if (obj == null) return EncodedNull.INSTANCE;
        if (obj instanceof EncodedValue value) return value;
        if (obj instanceof Boolean value) return EncodedBoolean.of(value);
        if (obj instanceof Byte value) return EncodedByte.of(value);
        if (obj instanceof Short value) return EncodedShort.of(value);
        if (obj instanceof Character value) return EncodedChar.of(value);
        if (obj instanceof Integer value) return EncodedInt.of(value);
        if (obj instanceof Float value) return EncodedFloat.of(value);
        if (obj instanceof Long value) return EncodedLong.of(value);
        if (obj instanceof Double value) return EncodedDouble.of(value);
        if (obj instanceof String value) return EncodedString.of(value);
        if (obj instanceof TypeId value) return EncodedType.of(value);
        if (obj instanceof ProtoId value) return EncodedMethodType.of(value);
        if (obj instanceof MethodId value) return EncodedMethod.of(value);
        if (obj instanceof FieldId value) return EncodedField.of(value);
        if (obj instanceof MethodHandleId value) return EncodedMethodHandle.of(value);
        if (obj instanceof CommonAnnotation value) return EncodedAnnotation.of(value);

        if (obj instanceof Class<?> value)
            return EncodedType.of(TypeId.of(value));
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
                "Unable to convert " + obj + " to EncodedValue");
    }
}
