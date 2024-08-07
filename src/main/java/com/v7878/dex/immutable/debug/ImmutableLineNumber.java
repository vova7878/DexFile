package com.v7878.dex.immutable.debug;

import com.v7878.dex.base.debug.BaseLineNumber;
import com.v7878.dex.iface.debug.LineNumber;
import com.v7878.dex.util.Preconditions;

public class ImmutableLineNumber extends BaseLineNumber implements ImmutableDebugItem {
    private final int line;

    protected ImmutableLineNumber(int line) {
        this.line = Preconditions.checkDebugLine(line);
    }

    public static ImmutableLineNumber of(int line) {
        return new ImmutableLineNumber(line);
    }

    public static ImmutableLineNumber of(LineNumber other) {
        if (other instanceof ImmutableLineNumber immutable) return immutable;
        return new ImmutableLineNumber(other.getLine());
    }

    @Override
    public int getLine() {
        return line;
    }
}
