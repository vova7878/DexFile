package com.v7878.dex.base;

import com.v7878.dex.iface.Dex;

import java.util.Objects;

public abstract class BaseDex implements Dex {
    @Override
    public int hashCode() {
        return Objects.hashCode(getClasses());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Dex other
                && Objects.equals(getClasses(), other.getClasses());
    }
}
