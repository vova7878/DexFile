package com.v7878.dex.base.bytecode;

import com.v7878.dex.iface.bytecode.SwitchElement;

import java.util.Objects;

public abstract class BaseSwitchElement implements SwitchElement {
    @Override
    public int hashCode() {
        return Objects.hash(getKey(), getOffset());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof SwitchElement other
                && getKey() == other.getKey()
                && getOffset() == other.getOffset();
    }
}
