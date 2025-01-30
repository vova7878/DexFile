package com.v7878.dex.immutable.debug;

import com.v7878.dex.util.Preconditions;

public final class EndLocal extends DebugItem {
    private final int register;

    private EndLocal(int register) {
        // TODO: cache values
        this.register = Preconditions.checkShortRegister(register);
    }

    public static EndLocal of(int register) {
        return new EndLocal(register);
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
        return obj instanceof EndLocal other
                && getRegister() == other.getRegister();
    }
}
