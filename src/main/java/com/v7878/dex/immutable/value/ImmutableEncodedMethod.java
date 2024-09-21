package com.v7878.dex.immutable.value;

import com.v7878.dex.ValueType;
import com.v7878.dex.base.value.BaseEncodedMethod;
import com.v7878.dex.iface.MethodId;
import com.v7878.dex.iface.value.EncodedMethod;
import com.v7878.dex.iface.value.EncodedValue;
import com.v7878.dex.immutable.ImmutableMethodId;
import com.v7878.dex.util.CollectionUtils;

import java.util.Objects;

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

    @Override
    public int hashCode() {
        return Objects.hashCode(getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof EncodedMethod other
                && Objects.equals(getValue(), other.getValue());
    }

    @Override
    public int compareTo(EncodedValue other) {
        if (other == this) return 0;
        int out = ValueType.compare(getValueType(), other.getValueType());
        if (out != 0) return out;
        return CollectionUtils.compareNonNull(getValue(), ((EncodedMethod) other).getValue());
    }
}
