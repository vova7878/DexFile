package com.v7878.dex.immutable.value;

import com.v7878.dex.base.value.BaseEncodedNull;
import com.v7878.dex.iface.value.EncodedNull;

import java.util.Objects;

public class ImmutableEncodedNull extends BaseEncodedNull implements ImmutableEncodedValue {
    public static final ImmutableEncodedNull INSTANCE = new ImmutableEncodedNull();

    protected ImmutableEncodedNull() {
    }

    public static ImmutableEncodedNull of(EncodedNull other) {
        Objects.requireNonNull(other);
        return INSTANCE;
    }
}
