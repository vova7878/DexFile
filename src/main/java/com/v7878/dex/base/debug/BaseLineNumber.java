package com.v7878.dex.base.debug;

import com.v7878.dex.iface.debug.LineNumber;

public abstract class BaseLineNumber implements LineNumber {
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
