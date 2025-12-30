package com.v7878.dex.util;

import com.v7878.dex.immutable.FieldId;
import com.v7878.dex.immutable.TypeId;

public class Ids {
    public static final TypeId THROWABLE = TypeId.of(Throwable.class);

    public static TypeId VOID_WRAPPER = TypeId.of(Void.class);
    public static TypeId BOOLEAN_WRAPPER = TypeId.of(Boolean.class);
    public static TypeId BYTE_WRAPPER = TypeId.of(Byte.class);
    public static TypeId SHORT_WRAPPER = TypeId.of(Short.class);
    public static TypeId CHAR_WRAPPER = TypeId.of(Character.class);
    public static TypeId INT_WRAPPER = TypeId.of(Integer.class);
    public static TypeId FLOAT_WRAPPER = TypeId.of(Float.class);
    public static TypeId LONG_WRAPPER = TypeId.of(Long.class);
    public static TypeId DOUBLE_WRAPPER = TypeId.of(Double.class);

    public static TypeId CLASS = TypeId.of(Class.class);

    public static FieldId VOID_TYPE = FieldId.of(VOID_WRAPPER, "TYPE", CLASS);
    public static FieldId BOOLEAN_TYPE = FieldId.of(BOOLEAN_WRAPPER, "TYPE", CLASS);
    public static FieldId BYTE_TYPE = FieldId.of(BYTE_WRAPPER, "TYPE", CLASS);
    public static FieldId SHORT_TYPE = FieldId.of(SHORT_WRAPPER, "TYPE", CLASS);
    public static FieldId CHAR_TYPE = FieldId.of(CHAR_WRAPPER, "TYPE", CLASS);
    public static FieldId INT_TYPE = FieldId.of(INT_WRAPPER, "TYPE", CLASS);
    public static FieldId FLOAT_TYPE = FieldId.of(FLOAT_WRAPPER, "TYPE", CLASS);
    public static FieldId LONG_TYPE = FieldId.of(LONG_WRAPPER, "TYPE", CLASS);
    public static FieldId DOUBLE_TYPE = FieldId.of(DOUBLE_WRAPPER, "TYPE", CLASS);
}
