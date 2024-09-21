package com.v7878.dex.immutable;

public abstract class MemberDef implements Annotatable {
    abstract String getName();

    abstract int getAccessFlags();

    abstract int getHiddenApiFlags();
}
