package com.v7878.dex.iface;

import java.util.Set;

public interface ClassDef extends Annotatable {
    TypeId getType();

    int getAccessFlags();

    TypeId getSuperclass();

    Set<? extends TypeId> getInterfaces();

    String getSourceFile();

    Set<? extends FieldDef> getFields();

    Set<? extends FieldDef> getStaticFields();

    Set<? extends FieldDef> getInstanceFields();

    Set<? extends MethodDef> getMethods();

    Set<? extends MethodDef> getDirectMethods();

    Set<? extends MethodDef> getVirtualMethods();
}
