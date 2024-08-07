package com.v7878.dex.immutable.value;

import com.v7878.dex.base.value.BaseEncodedField;
import com.v7878.dex.iface.FieldId;
import com.v7878.dex.iface.value.EncodedField;
import com.v7878.dex.immutable.ImmutableFieldId;

public class ImmutableEncodedField extends BaseEncodedField implements ImmutableEncodedValue {
    private final ImmutableFieldId value;

    protected ImmutableEncodedField(FieldId value) {
        this.value = ImmutableFieldId.of(value);
    }

    public static ImmutableEncodedField of(FieldId value) {
        return new ImmutableEncodedField(value);
    }

    public static ImmutableEncodedField of(EncodedField other) {
        if (other instanceof ImmutableEncodedField immutable) return immutable;
        return new ImmutableEncodedField(other.getValue());
    }

    @Override
    public ImmutableFieldId getValue() {
        return value;
    }
}
