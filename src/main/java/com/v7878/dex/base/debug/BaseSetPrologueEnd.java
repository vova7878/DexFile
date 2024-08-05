package com.v7878.dex.base.debug;

import com.v7878.dex.iface.debug.SetPrologueEnd;

public abstract class BaseSetPrologueEnd implements SetPrologueEnd {
    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof SetPrologueEnd;
    }
}
