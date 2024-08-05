package com.v7878.dex.base.debug;

import com.v7878.dex.iface.debug.AdvancePC;

public abstract class BaseAdvancePC implements AdvancePC {
    @Override
    public int hashCode() {
        return getAddrDiff();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof AdvancePC other
                && getAddrDiff() == other.getAddrDiff();
    }
}
