package com.v7878.dex.immutable.value;

import com.v7878.dex.base.value.BaseEncodedMethod;
import com.v7878.dex.iface.MethodId;
import com.v7878.dex.iface.value.EncodedMethod;
import com.v7878.dex.immutable.ImmutableMethodId;

public class ImmutableEncodedMethod extends BaseEncodedMethod implements ImmutableEncodedValue {
    private final ImmutableMethodId value;

    protected ImmutableEncodedMethod(MethodId value) {
        this.value = ImmutableMethodId.of(value);
    }

    public static ImmutableEncodedMethod of(MethodId value) {
        return new ImmutableEncodedMethod(value);
    }

    public static ImmutableEncodedMethod of(EncodedMethod other) {
        if (other instanceof ImmutableEncodedMethod immutable) return immutable;
        return new ImmutableEncodedMethod(other.getValue());
    }

    @Override
    public ImmutableMethodId getValue() {
        return value;
    }
}
