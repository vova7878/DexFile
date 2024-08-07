package com.v7878.dex.immutable.value;

import com.v7878.dex.base.value.BaseEncodedMethodType;
import com.v7878.dex.iface.ProtoId;
import com.v7878.dex.iface.value.EncodedMethodType;
import com.v7878.dex.immutable.ImmutableProtoId;

public class ImmutableEncodedMethodType extends BaseEncodedMethodType implements ImmutableEncodedValue {
    private final ImmutableProtoId value;

    protected ImmutableEncodedMethodType(ProtoId value) {
        this.value = ImmutableProtoId.of(value);
    }

    public static ImmutableEncodedMethodType of(ProtoId value) {
        return new ImmutableEncodedMethodType(value);
    }

    public static ImmutableEncodedMethodType of(EncodedMethodType other) {
        if (other instanceof ImmutableEncodedMethodType immutable) return immutable;
        return new ImmutableEncodedMethodType(other.getValue());
    }

    @Override
    public ImmutableProtoId getValue() {
        return value;
    }
}
