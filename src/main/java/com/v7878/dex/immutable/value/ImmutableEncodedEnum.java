package com.v7878.dex.immutable.value;

import com.v7878.dex.ValueType;
import com.v7878.dex.base.value.BaseEncodedEnum;
import com.v7878.dex.iface.FieldId;
import com.v7878.dex.iface.value.EncodedEnum;
import com.v7878.dex.iface.value.EncodedValue;
import com.v7878.dex.immutable.ImmutableFieldId;
import com.v7878.dex.util.CollectionUtils;

import java.util.Objects;

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

    @Override
    public int hashCode() {
        return Objects.hashCode(getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof EncodedEnum other
                && Objects.equals(getValue(), other.getValue());
    }

    @Override
    public int compareTo(EncodedValue other) {
        if (other == this) return 0;
        int out = ValueType.compare(getValueType(), other.getValueType());
        if (out != 0) return out;
        return CollectionUtils.compareNonNull(getValue(), ((EncodedEnum) other).getValue());
    }
}
