package com.v7878.dex.iface;

import com.v7878.dex.util.CollectionUtils;

import java.util.NavigableSet;

public interface ClassDef extends Annotatable {
    TypeId getType();

    int getAccessFlags();

    TypeId getSuperclass();

    NavigableSet<? extends TypeId> getInterfaces();

    String getSourceFile();

    NavigableSet<? extends FieldDef> getFields();

    default NavigableSet<? extends FieldDef> getStaticFields() {
        return CollectionUtils.getStaticFieldsSubset(getFields());
    }

    default NavigableSet<? extends FieldDef> getInstanceFields() {
        return CollectionUtils.getInstanceFieldsSubset(getFields());
    }

    NavigableSet<? extends MethodDef> getMethods();

    default NavigableSet<? extends MethodDef> getDirectMethods() {
        return CollectionUtils.getDirectMethodsSubset(getMethods());
    }

    default NavigableSet<? extends MethodDef> getVirtualMethods() {
        return CollectionUtils.getVirtualMethodsSubset(getMethods());
    }
}
