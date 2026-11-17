package com.v7878.dex;

import static com.v7878.dex.DexConstants.VERIFY_ERROR_ACCESS_CLASS;
import static com.v7878.dex.DexConstants.VERIFY_ERROR_ACCESS_FIELD;
import static com.v7878.dex.DexConstants.VERIFY_ERROR_ACCESS_METHOD;
import static com.v7878.dex.DexConstants.VERIFY_ERROR_CLASS_CHANGE;
import static com.v7878.dex.DexConstants.VERIFY_ERROR_GENERIC;
import static com.v7878.dex.DexConstants.VERIFY_ERROR_INSTANTIATION;
import static com.v7878.dex.DexConstants.VERIFY_ERROR_NO_CLASS;
import static com.v7878.dex.DexConstants.VERIFY_ERROR_NO_FIELD;
import static com.v7878.dex.DexConstants.VERIFY_ERROR_NO_METHOD;

public enum VerificationError {
    GENERIC(VERIFY_ERROR_GENERIC, "generic-error"),
    NO_SUCH_CLASS(VERIFY_ERROR_NO_CLASS, "no-such-class"),
    NO_SUCH_FIELD(VERIFY_ERROR_NO_FIELD, "no-such-field"),
    NO_SUCH_METHOD(VERIFY_ERROR_NO_METHOD, "no-such-method"),
    ILLEGAL_CLASS_ACCESS(VERIFY_ERROR_ACCESS_CLASS, "illegal-class-access"),
    ILLEGAL_FIELD_ACCESS(VERIFY_ERROR_ACCESS_FIELD, "illegal-field-access"),
    ILLEGAL_METHOD_ACCESS(VERIFY_ERROR_ACCESS_METHOD, "illegal-method-access"),
    CLASS_CHANGE(VERIFY_ERROR_CLASS_CHANGE, "class-change-error"),
    INSTANTIATION(VERIFY_ERROR_INSTANTIATION, "instantiation-error");

    private final int code;
    private final String name;

    VerificationError(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int code() {
        return code;
    }

    public static VerificationError of(String name) {
        return switch (name) {
            case "generic-error" -> GENERIC;
            case "no-such-class" -> NO_SUCH_CLASS;
            case "no-such-field" -> NO_SUCH_FIELD;
            case "no-such-method" -> NO_SUCH_METHOD;
            case "illegal-class-access" -> ILLEGAL_CLASS_ACCESS;
            case "illegal-field-access" -> ILLEGAL_FIELD_ACCESS;
            case "illegal-method-access" -> ILLEGAL_METHOD_ACCESS;
            case "class-change-error" -> CLASS_CHANGE;
            case "instantiation-error" -> INSTANTIATION;
            default -> throw new IllegalArgumentException(
                    "Unknown verification error: " + name);
        };
    }

    public static VerificationError of(int code) {
        return switch (code) {
            case VERIFY_ERROR_GENERIC -> GENERIC;
            case VERIFY_ERROR_NO_CLASS -> NO_SUCH_CLASS;
            case VERIFY_ERROR_NO_FIELD -> NO_SUCH_FIELD;
            case VERIFY_ERROR_NO_METHOD -> NO_SUCH_METHOD;
            case VERIFY_ERROR_ACCESS_CLASS -> ILLEGAL_CLASS_ACCESS;
            case VERIFY_ERROR_ACCESS_FIELD -> ILLEGAL_FIELD_ACCESS;
            case VERIFY_ERROR_ACCESS_METHOD -> ILLEGAL_METHOD_ACCESS;
            case VERIFY_ERROR_CLASS_CHANGE -> CLASS_CHANGE;
            case VERIFY_ERROR_INSTANTIATION -> INSTANTIATION;
            default -> throw new IllegalArgumentException(
                    "Unknown verification error: " + code);
        };
    }

    @Override
    public String toString() {
        return name;
    }
}
