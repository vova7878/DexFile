package com.v7878.dex.base;

import com.v7878.dex.iface.MethodDef;
import com.v7878.dex.iface.Parameter;
import com.v7878.dex.util.AccessFlagUtils;
import com.v7878.dex.util.CollectionUtils;

import java.util.Comparator;
import java.util.Objects;

public abstract class BaseMethodDef implements MethodDef {
    @Override
    public int hashCode() {
        return Objects.hash(getName(), getAccessFlags(), getHiddenApiFlags(),
                getReturnType(), getParameters(), getImplementation());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof MethodDef other
                && getAccessFlags() == other.getAccessFlags()
                && getHiddenApiFlags() == other.getHiddenApiFlags()
                && Objects.equals(getName(), other.getName())
                && Objects.equals(getReturnType(), other.getReturnType())
                && Objects.equals(getParameters(), other.getParameters())
                && Objects.equals(getImplementation(), other.getImplementation());
    }

    @Override
    public int compareTo(MethodDef other) {
        if (other == this) return 0;
        int out = Boolean.compare(AccessFlagUtils.isDirect(getAccessFlags()),
                AccessFlagUtils.isDirect(other.getAccessFlags()));
        if (out != 0) return out;
        out = CollectionUtils.compareNonNull(getName(), other.getName());
        if (out != 0) return out;
        out = CollectionUtils.compareNonNull(getReturnType(), other.getReturnType());
        if (out != 0) return out;
        return CollectionUtils.compareLexicographically(
                Comparator.comparing(Parameter::getType),
                getParameters(), other.getParameters());
    }
}
