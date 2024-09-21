package com.v7878.dex.immutable.debug;

import com.v7878.dex.util.Preconditions;

public class AdvancePC extends DebugItem {
    private final int addr_diff;

    protected AdvancePC(int addr_diff) {
        this.addr_diff = Preconditions.checkDebugAddrDiff(addr_diff);
    }

    public static AdvancePC of(int addr_diff) {
        return new AdvancePC(addr_diff);
    }

    public int getAddrDiff() {
        return addr_diff;
    }

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
