package com.v7878.dex.immutable.debug;

import com.v7878.dex.iface.debug.DebugItem;

public interface ImmutableDebugItem extends DebugItem {
    static ImmutableDebugItem of(DebugItem other) {
        throw new UnsupportedOperationException("TODO");
    }
}
