package com.v7878.dex.immutable;

public abstract sealed class MemberId permits MethodId, FieldId {
    public abstract TypeId getDeclaringClass();

    public abstract String getName();
}
