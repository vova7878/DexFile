package com.v7878.dex.immutable.value;

import com.v7878.dex.ValueType;
import com.v7878.dex.base.value.BaseEncodedType;
import com.v7878.dex.iface.TypeId;
import com.v7878.dex.iface.value.EncodedType;
import com.v7878.dex.iface.value.EncodedValue;
import com.v7878.dex.immutable.ImmutableTypeId;
import com.v7878.dex.util.CollectionUtils;

import java.util.Objects;

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

    @Override
    public int hashCode() {
        return Objects.hashCode(getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof EncodedType other
                && Objects.equals(getValue(), other.getValue());
    }

    @Override
    public int compareTo(EncodedValue other) {
        if (other == this) return 0;
        int out = ValueType.compare(getValueType(), other.getValueType());
        if (out != 0) return out;
        return CollectionUtils.compareNonNull(getValue(), ((EncodedType) other).getValue());
    }
}
