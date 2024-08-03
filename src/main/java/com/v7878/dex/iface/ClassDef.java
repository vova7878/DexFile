package com.v7878.dex.iface;

import java.util.NavigableSet;

public interface ClassDef extends Comparable<ClassDef>, Annotatable {
    TypeId getType();

    int getAccessFlags();

    TypeId getSuperclass();

    NavigableSet<? extends TypeId> getInterfaces();

    String getSourceFile();

    NavigableSet<? extends FieldDef> getStaticFields();

    NavigableSet<? extends FieldDef> getInstanceFields();

    NavigableSet<? extends FieldDef> getFields();

    NavigableSet<? extends MethodDef> getDirectMethods();

    NavigableSet<? extends MethodDef> getVirtualMethods();

    NavigableSet<? extends MethodDef> getMethods();
}
