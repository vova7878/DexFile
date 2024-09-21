package com.v7878.dex.immutable.value;

import com.v7878.dex.ValueType;
import com.v7878.dex.base.value.BaseEncodedNull;
import com.v7878.dex.iface.value.EncodedNull;
import com.v7878.dex.iface.value.EncodedValue;

import java.util.Objects;

public class ImmutableEncodedNull extends BaseEncodedNull implements ImmutableEncodedValue {
    public static final ImmutableEncodedNull INSTANCE = new ImmutableEncodedNull();

    protected ImmutableEncodedNull() {
    }

    public static ImmutableEncodedNull of(EncodedNull other) {
        Objects.requireNonNull(other);
        return INSTANCE;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof EncodedNull;
    }

    @Override
    public int compareTo(EncodedValue other) {
        if (other == this) return 0;
        return ValueType.compare(getValueType(), other.getValueType());
    }
}
