package com.v7878.dex.immutable;

public abstract sealed class MemberDef implements Annotatable permits MethodDef, FieldDef {
    abstract String getName();

    abstract int getAccessFlags();

    abstract int getHiddenApiFlags();
}
