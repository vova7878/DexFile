package com.v7878.dex.base;

import com.v7878.dex.iface.Parameter;

import java.util.Objects;

public abstract class BaseParameter implements Parameter {
    @Override
    public int hashCode() {
        return Objects.hash(getName(), getType(), getAnnotations());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Parameter other
                && Objects.equals(getName(), other.getName())
                && Objects.equals(getType(), other.getType())
                && Objects.equals(getAnnotations(), other.getAnnotations());
    }
}
