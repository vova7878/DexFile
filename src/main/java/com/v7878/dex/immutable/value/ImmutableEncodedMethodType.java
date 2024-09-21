package com.v7878.dex.immutable.value;

import com.v7878.dex.ValueType;
import com.v7878.dex.base.value.BaseEncodedMethodType;
import com.v7878.dex.iface.ProtoId;
import com.v7878.dex.iface.value.EncodedMethodType;
import com.v7878.dex.iface.value.EncodedValue;
import com.v7878.dex.immutable.ImmutableProtoId;
import com.v7878.dex.util.CollectionUtils;

import java.util.Objects;

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

    @Override
    public int hashCode() {
        return Objects.hashCode(getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof EncodedMethodType other
                && Objects.equals(getValue(), other.getValue());
    }

    @Override
    public int compareTo(EncodedValue other) {
        if (other == this) return 0;
        int out = ValueType.compare(getValueType(), other.getValueType());
        if (out != 0) return out;
        return CollectionUtils.compareNonNull(getValue(), ((EncodedMethodType) other).getValue());
    }
}
