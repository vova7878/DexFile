package com.v7878.dex;

import com.v7878.dex.immutable.CallSiteId;
import com.v7878.dex.immutable.FieldId;
import com.v7878.dex.immutable.MethodHandleId;
import com.v7878.dex.immutable.MethodId;
import com.v7878.dex.immutable.ProtoId;
import com.v7878.dex.immutable.TypeId;

import java.util.Objects;

public enum ReferenceType {
    STRING,
    TYPE,
    FIELD,
    METHOD,
    PROTO,
    CALLSITE,
    METHOD_HANDLE,
    RAW_INDEX;

    public static Object validate(ReferenceType type, Object value) {
        Objects.requireNonNull(value);
        return switch (type) {
            case STRING -> (String) value;
            case TYPE -> (TypeId) value;
            case FIELD -> (FieldId) value;
            case METHOD -> (MethodId) value;
            case PROTO -> (ProtoId) value;
            case CALLSITE -> (CallSiteId) value;
            case METHOD_HANDLE -> (MethodHandleId) value;
            case RAW_INDEX -> (Integer) value;
        };
    }

    public static String describe(ReferenceType type, Object value) {
        value = validate(type, value);
        return switch (type) {
            case STRING -> "\"" + value + "\"";
            case TYPE, FIELD, METHOD, PROTO, CALLSITE,
                 METHOD_HANDLE -> value.toString();
            case RAW_INDEX -> "@" + value;
        };
    }
}
