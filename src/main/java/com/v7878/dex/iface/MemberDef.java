package com.v7878.dex.iface;

public sealed interface MemberDef extends Annotatable permits FieldDef, MethodDef {
    String getName();

    int getAccessFlags();

    int getHiddenApiFlags();
}
