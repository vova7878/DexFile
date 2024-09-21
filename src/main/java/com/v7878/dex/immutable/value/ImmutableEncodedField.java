package com.v7878.dex.immutable.value;

import com.v7878.dex.ValueType;
import com.v7878.dex.base.value.BaseEncodedField;
import com.v7878.dex.iface.FieldId;
import com.v7878.dex.iface.value.EncodedField;
import com.v7878.dex.iface.value.EncodedValue;
import com.v7878.dex.immutable.ImmutableFieldId;
import com.v7878.dex.util.CollectionUtils;

import java.util.Objects;

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

    @Override
    public int hashCode() {
        return Objects.hashCode(getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof EncodedField other
                && Objects.equals(getValue(), other.getValue());
    }

    @Override
    public int compareTo(EncodedValue other) {
        if (other == this) return 0;
        int out = ValueType.compare(getValueType(), other.getValueType());
        if (out != 0) return out;
        return CollectionUtils.compareNonNull(getValue(), ((EncodedField) other).getValue());
    }
}
