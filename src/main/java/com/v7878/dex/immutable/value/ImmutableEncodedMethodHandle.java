package com.v7878.dex.immutable.value;

import com.v7878.dex.ValueType;
import com.v7878.dex.base.value.BaseEncodedMethodHandle;
import com.v7878.dex.iface.MethodHandleId;
import com.v7878.dex.iface.value.EncodedMethodHandle;
import com.v7878.dex.iface.value.EncodedValue;
import com.v7878.dex.immutable.ImmutableMethodHandleId;
import com.v7878.dex.util.CollectionUtils;

import java.util.Objects;

public class ImmutableEncodedMethodHandle extends BaseEncodedMethodHandle implements ImmutableEncodedValue {
    private final ImmutableMethodHandleId value;

    protected ImmutableEncodedMethodHandle(MethodHandleId value) {
        this.value = ImmutableMethodHandleId.of(value);
    }

    public static ImmutableEncodedMethodHandle of(MethodHandleId value) {
        return new ImmutableEncodedMethodHandle(value);
    }

    public static ImmutableEncodedMethodHandle of(EncodedMethodHandle other) {
        if (other instanceof ImmutableEncodedMethodHandle immutable) return immutable;
        return new ImmutableEncodedMethodHandle(other.getValue());
    }

    @Override
    public ImmutableMethodHandleId getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof EncodedMethodHandle other
                && Objects.equals(getValue(), other.getValue());
    }

    @Override
    public int compareTo(EncodedValue other) {
        if (other == this) return 0;
        int out = ValueType.compare(getValueType(), other.getValueType());
        if (out != 0) return out;
        return CollectionUtils.compareNonNull(getValue(), ((EncodedMethodHandle) other).getValue());
    }
}
