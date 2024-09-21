package com.v7878.dex.immutable.debug;

import com.v7878.dex.util.Preconditions;

public final class LineNumber extends DebugItem {
    private final int line;

    private LineNumber(int line) {
        this.line = Preconditions.checkDebugLine(line);
    }

    public static LineNumber of(int line) {
        return new LineNumber(line);
    }

    public int getLine() {
        return line;
    }

    @Override
    public int hashCode() {
        return getLine();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof LineNumber other
                && getLine() == other.getLine();
    }
}
