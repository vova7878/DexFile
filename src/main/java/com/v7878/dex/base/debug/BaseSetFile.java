package com.v7878.dex.base.debug;

import com.v7878.dex.iface.debug.SetFile;

import java.util.Objects;

public abstract class BaseSetFile implements SetFile {
    @Override
    public int hashCode() {
        return Objects.hashCode(getName());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof SetFile other
                && Objects.equals(getName(), other.getName());
    }
}
