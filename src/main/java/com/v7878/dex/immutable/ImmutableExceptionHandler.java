package com.v7878.dex.immutable;

import com.v7878.dex.base.BaseExceptionHandler;
import com.v7878.dex.iface.ExceptionHandler;
import com.v7878.dex.iface.TypeId;
import com.v7878.dex.util.Preconditions;

public class ImmutableExceptionHandler extends BaseExceptionHandler {
    private final ImmutableTypeId exception_type;
    private final int address;

    protected ImmutableExceptionHandler(TypeId exception_type, int address) {
        this.exception_type = ImmutableTypeId.of(exception_type);
        this.address = Preconditions.checkCodeAddress(address);
    }

    public static ImmutableExceptionHandler of(TypeId exception_type, int address) {
        return new ImmutableExceptionHandler(exception_type, address);
    }

    public static ImmutableExceptionHandler of(ExceptionHandler other) {
        if (other instanceof ImmutableExceptionHandler immutable) return immutable;
        return new ImmutableExceptionHandler(other.getExceptionType(), other.getAddress());
    }

    @Override
    public ImmutableTypeId getExceptionType() {
        return exception_type;
    }

    @Override
    public int getAddress() {
        return address;
    }
}
