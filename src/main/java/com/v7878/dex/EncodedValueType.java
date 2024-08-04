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

public enum EncodedValueType {
    BYTE(VALUE_BYTE),
    SHORT(VALUE_SHORT),
    CHAR(VALUE_CHAR),
    INT(VALUE_INT),
    LONG(VALUE_LONG),
    FLOAT(VALUE_FLOAT),
    DOUBLE(VALUE_DOUBLE),
    METHOD_TYPE(VALUE_METHOD_TYPE),
    METHOD_HANDLE(VALUE_METHOD_HANDLE),
    STRING(VALUE_STRING),
    TYPE(VALUE_TYPE),
    FIELD(VALUE_FIELD),
    METHOD(VALUE_METHOD),
    ENUM(VALUE_ENUM),
    ARRAY(VALUE_ARRAY),
    ANNOTATION(VALUE_ANNOTATION),
    NULL(VALUE_NULL),
    BOOLEAN(VALUE_BOOLEAN);

    private final int value;

    EncodedValueType(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    public static EncodedValueType of(int value) {
        for (EncodedValueType type : values()) {
            if (value == type.value) {
                return type;
            }
        }
        throw new IllegalStateException("Unknown encoded value type: " + value);
    }
}
