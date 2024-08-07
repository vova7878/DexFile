package com.v7878.dex.immutable.debug;

import com.v7878.dex.base.debug.BaseRestartLocal;
import com.v7878.dex.iface.debug.RestartLocal;
import com.v7878.dex.util.Preconditions;

public class ImmutableRestartLocal extends BaseRestartLocal implements ImmutableDebugItem {
    private final int register;

    protected ImmutableRestartLocal(int register) {
        this.register = Preconditions.checkShortRegister(register);
    }

    public static ImmutableRestartLocal of(int register) {
        return new ImmutableRestartLocal(register);
    }

    public static ImmutableRestartLocal of(RestartLocal other) {
        if (other instanceof ImmutableRestartLocal immutable) return immutable;
        return new ImmutableRestartLocal(other.getRegister());
    }

    @Override
    public int getRegister() {
        return register;
    }
}
