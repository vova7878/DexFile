package com.v7878.dex.immutable.debug;

import com.v7878.dex.base.debug.BaseEndLocal;
import com.v7878.dex.iface.debug.EndLocal;
import com.v7878.dex.util.Preconditions;

public class ImmutableEndLocal extends BaseEndLocal implements ImmutableDebugItem {
    private final int register;

    protected ImmutableEndLocal(int register) {
        this.register = Preconditions.checkShortRegister(register);
    }

    public static ImmutableEndLocal of(int register) {
        return new ImmutableEndLocal(register);
    }

    public static ImmutableEndLocal of(EndLocal other) {
        if (other instanceof ImmutableEndLocal immutable) return immutable;
        return new ImmutableEndLocal(other.getRegister());
    }

    @Override
    public int getRegister() {
        return register;
    }

    @Override
    public int hashCode() {
        return getRegister();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof EndLocal other
                && getRegister() == other.getRegister();
    }
}
