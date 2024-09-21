package com.v7878.dex.immutable;

public abstract sealed class MemberId permits MethodId, FieldId {
    abstract TypeId getDeclaringClass();

    abstract String getName();
}
