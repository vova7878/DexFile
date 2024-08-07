package com.v7878.dex.immutable.debug;

import com.v7878.dex.base.debug.BaseSetPrologueEnd;
import com.v7878.dex.iface.debug.SetPrologueEnd;

import java.util.Objects;

public class ImmutableSetPrologueEnd extends BaseSetPrologueEnd implements ImmutableDebugItem {
    public static final ImmutableSetPrologueEnd INSTANCE = new ImmutableSetPrologueEnd();

    protected ImmutableSetPrologueEnd() {
    }

    public static ImmutableSetPrologueEnd of(SetPrologueEnd other) {
        Objects.requireNonNull(other);
        return INSTANCE;
    }
}
