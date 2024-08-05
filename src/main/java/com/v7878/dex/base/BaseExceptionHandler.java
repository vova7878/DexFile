package com.v7878.dex.base;

import com.v7878.dex.iface.ExceptionHandler;

import java.util.Objects;

public abstract class BaseExceptionHandler implements ExceptionHandler {
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
