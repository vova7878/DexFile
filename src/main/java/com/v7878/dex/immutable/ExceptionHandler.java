package com.v7878.dex.immutable;

import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public class ExceptionHandler {
    private final TypeId exception_type;
    private final int address;

    protected ExceptionHandler(TypeId exception_type, int address) {
        this.exception_type = Objects.requireNonNull(exception_type);
        this.address = Preconditions.checkCodeAddress(address);
    }

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
}
