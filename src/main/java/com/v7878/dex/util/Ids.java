package com.v7878.dex.util;

import com.v7878.dex.immutable.FieldId;
import com.v7878.dex.immutable.TypeId;

import java.io.Serializable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

public class Ids {
    public static final TypeId THROWABLE = TypeId.of(Throwable.class);

    public static final TypeId VOID_WRAPPER = TypeId.of(Void.class);
    public static final TypeId BOOLEAN_WRAPPER = TypeId.of(Boolean.class);
    public static final TypeId BYTE_WRAPPER = TypeId.of(Byte.class);
    public static final TypeId SHORT_WRAPPER = TypeId.of(Short.class);
    public static final TypeId CHAR_WRAPPER = TypeId.of(Character.class);
    public static final TypeId INT_WRAPPER = TypeId.of(Integer.class);
    public static final TypeId FLOAT_WRAPPER = TypeId.of(Float.class);
    public static final TypeId LONG_WRAPPER = TypeId.of(Long.class);
    public static final TypeId DOUBLE_WRAPPER = TypeId.of(Double.class);

    public static final TypeId STRING = TypeId.of(String.class);
    public static final TypeId CLASS = TypeId.of(Class.class);
    public static final TypeId METHOD_TYPE = TypeId.of(MethodType.class);
    public static final TypeId METHOD_HANDLE = TypeId.of(MethodHandle.class);

    public static final TypeId CLONEABLE = TypeId.of(Cloneable.class);
    public static final TypeId SERIALIZABLE = TypeId.of(Serializable.class);

    public static final FieldId VOID_TYPE = FieldId.of(VOID_WRAPPER, "TYPE", CLASS);
    public static final FieldId BOOLEAN_TYPE = FieldId.of(BOOLEAN_WRAPPER, "TYPE", CLASS);
    public static final FieldId BYTE_TYPE = FieldId.of(BYTE_WRAPPER, "TYPE", CLASS);
    public static final FieldId SHORT_TYPE = FieldId.of(SHORT_WRAPPER, "TYPE", CLASS);
    public static final FieldId CHAR_TYPE = FieldId.of(CHAR_WRAPPER, "TYPE", CLASS);
    public static final FieldId INT_TYPE = FieldId.of(INT_WRAPPER, "TYPE", CLASS);
    public static final FieldId FLOAT_TYPE = FieldId.of(FLOAT_WRAPPER, "TYPE", CLASS);
    public static final FieldId LONG_TYPE = FieldId.of(LONG_WRAPPER, "TYPE", CLASS);
    public static final FieldId DOUBLE_TYPE = FieldId.of(DOUBLE_WRAPPER, "TYPE", CLASS);
}
