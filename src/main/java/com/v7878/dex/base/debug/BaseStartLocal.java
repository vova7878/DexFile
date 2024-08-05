package com.v7878.dex.base.debug;

import com.v7878.dex.iface.debug.StartLocal;

import java.util.Objects;

public abstract class BaseStartLocal implements StartLocal {
    @Override
    public int hashCode() {
        return Objects.hash(getRegister(), getName(), getType(), getSignature());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof StartLocal other
                && getRegister() == other.getRegister()
                && Objects.equals(getName(), other.getName())
                && Objects.equals(getType(), other.getType())
                && Objects.equals(getSignature(), other.getSignature());
    }
}
