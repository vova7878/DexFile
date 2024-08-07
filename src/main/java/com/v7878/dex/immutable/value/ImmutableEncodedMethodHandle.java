package com.v7878.dex.immutable.value;

import com.v7878.dex.base.value.BaseEncodedMethodHandle;
import com.v7878.dex.iface.MethodHandleId;
import com.v7878.dex.iface.value.EncodedMethodHandle;
import com.v7878.dex.immutable.ImmutableMethodHandleId;

public class ImmutableEncodedMethodHandle extends BaseEncodedMethodHandle implements ImmutableEncodedValue {
    private final ImmutableMethodHandleId value;

    protected ImmutableEncodedMethodHandle(MethodHandleId value) {
        this.value = ImmutableMethodHandleId.of(value);
    }

    public static ImmutableEncodedMethodHandle of(MethodHandleId value) {
        return new ImmutableEncodedMethodHandle(value);
    }

    public static ImmutableEncodedMethodHandle of(EncodedMethodHandle other) {
        if (other instanceof ImmutableEncodedMethodHandle immutable) return immutable;
        return new ImmutableEncodedMethodHandle(other.getValue());
    }

    @Override
    public ImmutableMethodHandleId getValue() {
        return value;
    }
}
