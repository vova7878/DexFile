package com.v7878.dex;

import static com.v7878.dex.DexConstants.METHOD_HANDLE_TYPE_INSTANCE_GET;
import static com.v7878.dex.DexConstants.METHOD_HANDLE_TYPE_INSTANCE_PUT;
import static com.v7878.dex.DexConstants.METHOD_HANDLE_TYPE_INVOKE_CONSTRUCTOR;
import static com.v7878.dex.DexConstants.METHOD_HANDLE_TYPE_INVOKE_DIRECT;
import static com.v7878.dex.DexConstants.METHOD_HANDLE_TYPE_INVOKE_INSTANCE;
import static com.v7878.dex.DexConstants.METHOD_HANDLE_TYPE_INVOKE_INTERFACE;
import static com.v7878.dex.DexConstants.METHOD_HANDLE_TYPE_INVOKE_STATIC;
import static com.v7878.dex.DexConstants.METHOD_HANDLE_TYPE_STATIC_GET;
import static com.v7878.dex.DexConstants.METHOD_HANDLE_TYPE_STATIC_PUT;

public enum MethodHandleType {
    STATIC_PUT(METHOD_HANDLE_TYPE_STATIC_PUT, false),
    STATIC_GET(METHOD_HANDLE_TYPE_STATIC_GET, false),
    INSTANCE_PUT(METHOD_HANDLE_TYPE_INSTANCE_PUT, false),
    INSTANCE_GET(METHOD_HANDLE_TYPE_INSTANCE_GET, false),
    INVOKE_STATIC(METHOD_HANDLE_TYPE_INVOKE_STATIC, true),
    INVOKE_INSTANCE(METHOD_HANDLE_TYPE_INVOKE_INSTANCE, true),
    INVOKE_CONSTRUCTOR(METHOD_HANDLE_TYPE_INVOKE_CONSTRUCTOR, true),
    INVOKE_DIRECT(METHOD_HANDLE_TYPE_INVOKE_DIRECT, true),
    INVOKE_INTERFACE(METHOD_HANDLE_TYPE_INVOKE_INTERFACE, true);

    private final int value;
    private final boolean isMethod;

    MethodHandleType(int value, boolean isMethod) {
        this.value = value;
        this.isMethod = isMethod;
    }

    public int value() {
        return value;
    }

    public boolean isMethodAccess() {
        return isMethod;
    }

    public boolean isFieldAccess() {
        return !isMethod;
    }

    public static MethodHandleType of(int value) {
        for (MethodHandleType type : values()) {
            if (value == type.value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown method handle type: " + value);
    }

    public static int compare(MethodHandleType left, MethodHandleType right) {
        return Integer.compare(left.value(), right.value());
    }
}
