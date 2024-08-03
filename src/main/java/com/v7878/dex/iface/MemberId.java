package com.v7878.dex.iface;

public sealed interface MemberId permits FieldId, MethodId {
    TypeId getDeclaringClass();

    String getName();
}
