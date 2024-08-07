package com.v7878.dex.immutable.debug;

import com.v7878.dex.iface.debug.AdvancePC;
import com.v7878.dex.iface.debug.DebugItem;
import com.v7878.dex.iface.debug.EndLocal;
import com.v7878.dex.iface.debug.LineNumber;
import com.v7878.dex.iface.debug.RestartLocal;
import com.v7878.dex.iface.debug.SetEpilogueBegin;
import com.v7878.dex.iface.debug.SetFile;
import com.v7878.dex.iface.debug.SetPrologueEnd;
import com.v7878.dex.iface.debug.StartLocal;

public interface ImmutableDebugItem extends DebugItem {
    static ImmutableDebugItem of(DebugItem other) {
        if (other instanceof ImmutableDebugItem immutable) return immutable;
        if (other instanceof AdvancePC item) return ImmutableAdvancePC.of(item);
        if (other instanceof LineNumber item) return ImmutableLineNumber.of(item);
        if (other instanceof StartLocal item) return ImmutableStartLocal.of(item);
        if (other instanceof EndLocal item) return ImmutableEndLocal.of(item);
        if (other instanceof RestartLocal item) return ImmutableRestartLocal.of(item);
        if (other instanceof SetFile item) return ImmutableSetFile.of(item);
        if (other instanceof SetPrologueEnd item) return ImmutableSetPrologueEnd.of(item);
        if (other instanceof SetEpilogueBegin item) return ImmutableSetEpilogueBegin.of(item);
        throw new IllegalArgumentException("Invalid debug item type");
    }
}
