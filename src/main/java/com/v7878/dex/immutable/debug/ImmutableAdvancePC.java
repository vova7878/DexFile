package com.v7878.dex.immutable.debug;

import com.v7878.dex.base.debug.BaseAdvancePC;
import com.v7878.dex.iface.debug.AdvancePC;
import com.v7878.dex.util.Preconditions;

public class ImmutableAdvancePC extends BaseAdvancePC implements ImmutableDebugItem {
    private final int addr_diff;

    protected ImmutableAdvancePC(int addr_diff) {
        this.addr_diff = Preconditions.checkDebugAddrDiff(addr_diff);
    }

    public static ImmutableAdvancePC of(int addr_diff) {
        return new ImmutableAdvancePC(addr_diff);
    }

    public static ImmutableAdvancePC of(AdvancePC other) {
        if (other instanceof ImmutableAdvancePC immutable) return immutable;
        return new ImmutableAdvancePC(other.getAddrDiff());
    }

    @Override
    public int getAddrDiff() {
        return addr_diff;
    }
}
