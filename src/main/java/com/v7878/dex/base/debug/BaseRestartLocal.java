package com.v7878.dex.base.debug;

import com.v7878.dex.iface.debug.RestartLocal;

public abstract class BaseRestartLocal implements RestartLocal {
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
