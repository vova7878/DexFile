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
    STATIC_PUT(METHOD_HANDLE_TYPE_STATIC_PUT, "static-put", false),
    STATIC_GET(METHOD_HANDLE_TYPE_STATIC_GET, "static-get", false),
    INSTANCE_PUT(METHOD_HANDLE_TYPE_INSTANCE_PUT, "instance-put", false),
    INSTANCE_GET(METHOD_HANDLE_TYPE_INSTANCE_GET, "instance-get", false),
    INVOKE_STATIC(METHOD_HANDLE_TYPE_INVOKE_STATIC, "invoke-static", true),
    INVOKE_INSTANCE(METHOD_HANDLE_TYPE_INVOKE_INSTANCE, "invoke-instance", true),
    INVOKE_CONSTRUCTOR(METHOD_HANDLE_TYPE_INVOKE_CONSTRUCTOR, "invoke-constructor", true),
    INVOKE_DIRECT(METHOD_HANDLE_TYPE_INVOKE_DIRECT, "invoke-direct", true),
    INVOKE_INTERFACE(METHOD_HANDLE_TYPE_INVOKE_INTERFACE, "invoke-interface", true);

    private final int value;
    private final String name;
    private final boolean isMethod;

    MethodHandleType(int value, String name, boolean isMethod) {
        this.value = value;
        this.name = name;
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

    public static MethodHandleType of(String name) {
        return switch (name) {
            case "static-put" -> STATIC_PUT;
            case "static-get" -> STATIC_GET;
            case "instance-put" -> INSTANCE_PUT;
            case "instance-get" -> INSTANCE_GET;
            case "invoke-static" -> INVOKE_STATIC;
            case "invoke-instance" -> INVOKE_INSTANCE;
            case "invoke-constructor" -> INVOKE_CONSTRUCTOR;
            case "invoke-direct" -> INVOKE_DIRECT;
            case "invoke-interface" -> INVOKE_INTERFACE;
            default -> throw new IllegalArgumentException(
                    "Unknown method handle type: " + name);
        };
    }

    public static MethodHandleType of(int value) {
        return switch (value) {
            case METHOD_HANDLE_TYPE_STATIC_PUT -> STATIC_PUT;
            case METHOD_HANDLE_TYPE_STATIC_GET -> STATIC_GET;
            case METHOD_HANDLE_TYPE_INSTANCE_PUT -> INSTANCE_PUT;
            case METHOD_HANDLE_TYPE_INSTANCE_GET -> INSTANCE_GET;
            case METHOD_HANDLE_TYPE_INVOKE_STATIC -> INVOKE_STATIC;
            case METHOD_HANDLE_TYPE_INVOKE_INSTANCE -> INVOKE_INSTANCE;
            case METHOD_HANDLE_TYPE_INVOKE_CONSTRUCTOR -> INVOKE_CONSTRUCTOR;
            case METHOD_HANDLE_TYPE_INVOKE_DIRECT -> INVOKE_DIRECT;
            case METHOD_HANDLE_TYPE_INVOKE_INTERFACE -> INVOKE_INTERFACE;
            default -> throw new IllegalArgumentException(
                    "Unknown method handle type: " + value);
        };
    }

    public static int compare(MethodHandleType left, MethodHandleType right) {
        return Integer.compare(left.value(), right.value());
    }

    @Override
    public String toString() {
        return name;
    }
}
