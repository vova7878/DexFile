package com.v7878.dex;

import static com.v7878.dex.DexConstants.VALUE_ANNOTATION;
import static com.v7878.dex.DexConstants.VALUE_ARRAY;
import static com.v7878.dex.DexConstants.VALUE_BOOLEAN;
import static com.v7878.dex.DexConstants.VALUE_BYTE;
import static com.v7878.dex.DexConstants.VALUE_CHAR;
import static com.v7878.dex.DexConstants.VALUE_DOUBLE;
import static com.v7878.dex.DexConstants.VALUE_ENUM;
import static com.v7878.dex.DexConstants.VALUE_FIELD;
import static com.v7878.dex.DexConstants.VALUE_FLOAT;
import static com.v7878.dex.DexConstants.VALUE_INT;
import static com.v7878.dex.DexConstants.VALUE_LONG;
import static com.v7878.dex.DexConstants.VALUE_METHOD;
import static com.v7878.dex.DexConstants.VALUE_METHOD_HANDLE;
import static com.v7878.dex.DexConstants.VALUE_METHOD_TYPE;
import static com.v7878.dex.DexConstants.VALUE_NULL;
import static com.v7878.dex.DexConstants.VALUE_SHORT;
import static com.v7878.dex.DexConstants.VALUE_STRING;
import static com.v7878.dex.DexConstants.VALUE_TYPE;

import com.v7878.dex.immutable.TypeId;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;

public enum ValueType {
    BOOLEAN(VALUE_BOOLEAN, TypeId.Z),
    BYTE(VALUE_BYTE, TypeId.B),
    SHORT(VALUE_SHORT, TypeId.S),
    CHAR(VALUE_CHAR, TypeId.C),
    INT(VALUE_INT, TypeId.I),
    FLOAT(VALUE_FLOAT, TypeId.F),
    LONG(VALUE_LONG, TypeId.J),
    DOUBLE(VALUE_DOUBLE, TypeId.D),
    METHOD_TYPE(VALUE_METHOD_TYPE, TypeId.of(MethodType.class)),
    METHOD_HANDLE(VALUE_METHOD_HANDLE, TypeId.of(MethodHandle.class)),
    STRING(VALUE_STRING, TypeId.of(String.class)),
    TYPE(VALUE_TYPE, TypeId.of(Class.class)),
    FIELD(VALUE_FIELD, TypeId.of(Field.class)),
    // Even though the value type is called a "method", it is a
    // single entity for methods and constructors, i.e. Executable
    METHOD(VALUE_METHOD, TypeId.of(Executable.class)),
    ENUM(VALUE_ENUM, TypeId.of(Enum.class)),
    // This is an untyped array; It can contain values of different types,
    // and Java doesn't have a similar runtime construct.
    // There isn't even a common supertype for all arrays
    ARRAY(VALUE_ARRAY, TypeId.of(Array.class)),
    ANNOTATION(VALUE_ANNOTATION, TypeId.of(Annotation.class)),
    NULL(VALUE_NULL, TypeId.of(Object.class));

    private final int value;
    private final TypeId type;

    ValueType(int value, TypeId type) {
        this.value = value;
        this.type = type;
    }

    public int value() {
        return value;
    }

    public TypeId getType() {
        return type;
    }

    public static ValueType of(int value) {
        return switch (value) {
            case VALUE_BYTE -> BYTE;
            case VALUE_SHORT -> SHORT;
            case VALUE_CHAR -> CHAR;
            case VALUE_INT -> INT;
            case VALUE_LONG -> LONG;
            case VALUE_FLOAT -> FLOAT;
            case VALUE_DOUBLE -> DOUBLE;
            case VALUE_METHOD_TYPE -> METHOD_TYPE;
            case VALUE_METHOD_HANDLE -> METHOD_HANDLE;
            case VALUE_STRING -> STRING;
            case VALUE_TYPE -> TYPE;
            case VALUE_FIELD -> FIELD;
            case VALUE_METHOD -> METHOD;
            case VALUE_ENUM -> ENUM;
            case VALUE_ARRAY -> ARRAY;
            case VALUE_ANNOTATION -> ANNOTATION;
            case VALUE_NULL -> NULL;
            case VALUE_BOOLEAN -> BOOLEAN;
            default -> throw new IllegalArgumentException(
                    "Unknown encoded value type: " + value);
        };
    }

    public static int compare(ValueType left, ValueType right) {
        return Integer.compare(left.value(), right.value());
    }
}
