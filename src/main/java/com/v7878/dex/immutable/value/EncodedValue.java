package com.v7878.dex.immutable.value;

import static com.v7878.dex.util.ShortyUtils.invalidShorty;

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
import java.util.ArrayList;
import java.util.Collection;

public abstract sealed class EncodedValue implements Comparable<EncodedValue>
        permits EncodedAnnotation, EncodedArray, EncodedBoolean, EncodedByte,
        EncodedChar, EncodedDouble, EncodedEnum, EncodedField, EncodedFloat,
        EncodedInt, EncodedLong, EncodedMethod, EncodedMethodHandle, EncodedMethodType,
        EncodedNull, EncodedShort, EncodedString, EncodedType {
    public abstract ValueType getValueType();

    public TypeId getType() {
        return getValueType().getType();
    }

    public abstract boolean isDefault();

    public static EncodedValue defaultValue(TypeId type) {
        char shorty = type.getShorty();
        return switch (shorty) {
            case 'Z' -> EncodedBoolean.FALSE;
            case 'B' -> EncodedByte.of((byte) 0);
            case 'S' -> EncodedShort.of((short) 0);
            case 'C' -> EncodedChar.of((char) 0);
            case 'I' -> EncodedInt.of(0);
            case 'J' -> EncodedLong.of(0);
            case 'F' -> EncodedFloat.of(0);
            case 'D' -> EncodedDouble.of(0);
            case 'L' -> EncodedNull.INSTANCE;
            // V - void
            default -> throw invalidShorty(shorty);
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

        if (obj instanceof Class<?> value) return EncodedType.of(TypeId.of(value));
        if (obj instanceof MethodType value) return EncodedMethodType.of(ProtoId.of(value));
        if (obj instanceof Executable value) return EncodedMethod.of(MethodId.of(value));
        if (obj instanceof Field value) return EncodedField.of(FieldId.of(value));
        if (obj instanceof Enum<?> value) return EncodedEnum.of(FieldId.of(value));
        if (obj instanceof MethodHandle) {
            //TODO
            throw new UnsupportedOperationException("Not implemented yet");
        }
        if (obj.getClass().isAnnotation()) {
            //TODO
            throw new UnsupportedOperationException("Not implemented yet");
        }
        // TODO: use raw list constructor for arrays
        if (obj instanceof boolean[] array) {
            var list = new ArrayList<EncodedValue>(array.length);
            for (var value : array) {
                list.add(EncodedBoolean.of(value));
            }
            return EncodedArray.of(list);
        }
        if (obj instanceof byte[] array) {
            var list = new ArrayList<EncodedValue>(array.length);
            for (var value : array) {
                list.add(EncodedByte.of(value));
            }
            return EncodedArray.of(list);
        }
        if (obj instanceof short[] array) {
            var list = new ArrayList<EncodedValue>(array.length);
            for (var value : array) {
                list.add(EncodedShort.of(value));
            }
            return EncodedArray.of(list);
        }
        if (obj instanceof char[] array) {
            var list = new ArrayList<EncodedValue>(array.length);
            for (var value : array) {
                list.add(EncodedChar.of(value));
            }
            return EncodedArray.of(list);
        }
        if (obj instanceof int[] array) {
            var list = new ArrayList<EncodedValue>(array.length);
            for (var value : array) {
                list.add(EncodedInt.of(value));
            }
            return EncodedArray.of(list);
        }
        if (obj instanceof float[] array) {
            var list = new ArrayList<EncodedValue>(array.length);
            for (var value : array) {
                list.add(EncodedFloat.of(value));
            }
            return EncodedArray.of(list);
        }
        if (obj instanceof long[] array) {
            var list = new ArrayList<EncodedValue>(array.length);
            for (var value : array) {
                list.add(EncodedLong.of(value));
            }
            return EncodedArray.of(list);
        }
        if (obj instanceof double[] array) {
            var list = new ArrayList<EncodedValue>(array.length);
            for (var value : array) {
                list.add(EncodedDouble.of(value));
            }
            return EncodedArray.of(list);
        }
        if (obj instanceof Object[] array) {
            var list = new ArrayList<EncodedValue>(array.length);
            for (var value : array) {
                list.add(EncodedValue.ofValue(value));
            }
            return EncodedArray.of(list);
        }
        if (obj instanceof Iterable<?> iterable) {
            ArrayList<EncodedValue> list = iterable instanceof Collection<?> collection ?
                    new ArrayList<>(collection.size()) : new ArrayList<>();
            for (var value : iterable) {
                list.add(EncodedValue.ofValue(value));
            }
            return EncodedArray.of(list);
        }
        throw new IllegalArgumentException(
                "Unable to convert " + obj + " to EncodedValue");
    }
}
