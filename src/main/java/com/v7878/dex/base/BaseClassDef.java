package com.v7878.dex.base;

import com.v7878.dex.iface.ClassDef;

import java.util.Objects;

public abstract class BaseClassDef implements ClassDef {
    @Override
    public int hashCode() {
        return Objects.hash(getType(), getAccessFlags(), getSuperclass(),
                getInterfaces(), getSourceFile(), getFields(), getMethods());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof ClassDef other
                && getAccessFlags() == other.getAccessFlags()
                && Objects.equals(getType(), other.getType())
                && Objects.equals(getSuperclass(), other.getSuperclass())
                && Objects.equals(getSourceFile(), other.getSourceFile())
                && Objects.equals(getInterfaces(), other.getInterfaces())
                && Objects.equals(getFields(), other.getFields())
                && Objects.equals(getMethods(), other.getMethods());
    }
}
