package com.v7878.dex.immutable.value;

import com.v7878.dex.base.value.BaseEncodedType;
import com.v7878.dex.iface.TypeId;
import com.v7878.dex.iface.value.EncodedType;
import com.v7878.dex.immutable.ImmutableTypeId;

public class ImmutableEncodedType extends BaseEncodedType implements ImmutableEncodedValue {
    private final ImmutableTypeId value;

    protected ImmutableEncodedType(TypeId value) {
        this.value = ImmutableTypeId.of(value);
    }

    public static ImmutableEncodedType of(TypeId value) {
        return new ImmutableEncodedType(value);
    }

    public static ImmutableEncodedType of(EncodedType other) {
        if (other instanceof ImmutableEncodedType immutable) return immutable;
        return new ImmutableEncodedType(other.getValue());
    }

    @Override
    public ImmutableTypeId getValue() {
        return value;
    }
}
