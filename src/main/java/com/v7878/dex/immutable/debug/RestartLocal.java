package com.v7878.dex.immutable.debug;

import com.v7878.dex.util.Preconditions;

public final class RestartLocal extends DebugItem {
    private final int register;

    private RestartLocal(int register) {
        // TODO: cache values
        this.register = Preconditions.checkShortRegister(register);
    }

    public static RestartLocal of(int register) {
        return new RestartLocal(register);
    }

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
        return obj instanceof RestartLocal other
                && getRegister() == other.getRegister();
    }
}
