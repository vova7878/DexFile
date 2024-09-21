package com.v7878.dex.immutable;

public abstract sealed class MemberDef implements Annotatable permits MethodDef, FieldDef {
    public abstract String getName();

    public abstract int getAccessFlags();

    public abstract int getHiddenApiFlags();
}
