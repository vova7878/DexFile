package com.v7878.dex.base;

import com.v7878.dex.iface.ClassDef;
import com.v7878.dex.iface.FieldDef;
import com.v7878.dex.iface.MethodDef;
import com.v7878.dex.util.CollectionUtils;

import java.util.NavigableSet;
import java.util.Objects;

public abstract class BaseClassDef implements ClassDef {
    public NavigableSet<? extends FieldDef> getStaticFields() {
        return CollectionUtils.getStaticFieldsSubset(getFields());
    }

    public NavigableSet<? extends FieldDef> getInstanceFields() {
        return CollectionUtils.getInstanceFieldsSubset(getFields());
    }

    public NavigableSet<? extends MethodDef> getDirectMethods() {
        return CollectionUtils.getDirectMethodsSubset(getMethods());
    }

    public NavigableSet<? extends MethodDef> getVirtualMethods() {
        return CollectionUtils.getVirtualMethodsSubset(getMethods());
    }

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
