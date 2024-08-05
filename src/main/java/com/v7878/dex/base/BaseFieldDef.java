package com.v7878.dex.base;

import com.v7878.dex.iface.FieldDef;
import com.v7878.dex.util.AccessFlagUtils;
import com.v7878.dex.util.CollectionUtils;

import java.util.Objects;

public abstract class BaseFieldDef implements FieldDef {
    @Override
    public int hashCode() {
        return Objects.hash(getName(), getAccessFlags(),
                getHiddenApiFlags(), getType(), getInitialValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof FieldDef other
                && getAccessFlags() == other.getAccessFlags()
                && getHiddenApiFlags() == other.getHiddenApiFlags()
                && Objects.equals(getName(), other.getName())
                && Objects.equals(getType(), other.getType())
                && Objects.equals(getInitialValue(), other.getInitialValue());
    }

    @Override
    public int compareTo(FieldDef other) {
        if (other == this) return 0;
        int out = Boolean.compare(AccessFlagUtils.isStatic(getAccessFlags()),
                AccessFlagUtils.isStatic(other.getAccessFlags()));
        if (out != 0) return out;
        out = CollectionUtils.compareNonNull(getName(), other.getName());
        if (out != 0) return out;
        return CollectionUtils.compareNonNull(getType(), other.getType());
    }
}
