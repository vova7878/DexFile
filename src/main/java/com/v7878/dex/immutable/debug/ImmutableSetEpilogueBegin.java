package com.v7878.dex.immutable.debug;

import com.v7878.dex.base.debug.BaseSetEpilogueBegin;
import com.v7878.dex.iface.debug.SetEpilogueBegin;

import java.util.Objects;

public class ImmutableSetEpilogueBegin extends BaseSetEpilogueBegin implements ImmutableDebugItem {
    public static final ImmutableSetEpilogueBegin INSTANCE = new ImmutableSetEpilogueBegin();

    protected ImmutableSetEpilogueBegin() {
    }

    public static ImmutableSetEpilogueBegin of(SetEpilogueBegin other) {
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
        return obj instanceof SetEpilogueBegin;
    }
}
