package com.v7878.dex.immutable.value;

import com.v7878.dex.base.value.BaseEncodedEnum;
import com.v7878.dex.iface.FieldId;
import com.v7878.dex.iface.value.EncodedEnum;
import com.v7878.dex.immutable.ImmutableFieldId;

public class ImmutableEncodedEnum extends BaseEncodedEnum implements ImmutableEncodedValue {
    private final ImmutableFieldId value;

    protected ImmutableEncodedEnum(FieldId value) {
        this.value = ImmutableFieldId.of(value);
    }

    public static ImmutableEncodedEnum of(FieldId value) {
        return new ImmutableEncodedEnum(value);
    }

    public static ImmutableEncodedEnum of(EncodedEnum other) {
        if (other instanceof ImmutableEncodedEnum immutable) return immutable;
        return new ImmutableEncodedEnum(other.getValue());
    }

    @Override
    public ImmutableFieldId getValue() {
        return value;
    }
}
