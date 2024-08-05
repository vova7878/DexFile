package com.v7878.dex.base.debug;

import com.v7878.dex.iface.debug.EndLocal;

public abstract class BaseEndLocal implements EndLocal {
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
