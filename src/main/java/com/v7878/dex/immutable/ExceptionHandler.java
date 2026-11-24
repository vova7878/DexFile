package com.v7878.dex.immutable;

import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public final class ExceptionHandler {
    private final TypeId exception_type;
    private final int address;

    private ExceptionHandler(TypeId exception_type, int address) {
        this.exception_type = Objects.requireNonNull(exception_type);
        this.address = Preconditions.checkCodeAddress(address);
    }

    /**
     * Creates a handler for {@code exception_type}. The model keeps the type
     * as-is: ART accepts a primitive, array or other non-throwable reference
     * type as long as the handler does not execute {@code move-exception}.
     */
    public static ExceptionHandler of(TypeId exception_type, int address) {
        return new ExceptionHandler(exception_type, address);
    }

    public TypeId getExceptionType() {
        return exception_type;
    }

    public int getAddress() {
        return address;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAddress(), getExceptionType());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof ExceptionHandler other
                && getAddress() == other.getAddress()
                && Objects.equals(getExceptionType(), other.getExceptionType());
    }

    /**
     * Debug view, not part of the API contract.
     */
    @Override
    public String toString() {
        // TODO: replace with the smali printer
        return "ExceptionHandler{type=" + getExceptionType()
                + " address=" + getAddress()
                + "}";
    }
}
