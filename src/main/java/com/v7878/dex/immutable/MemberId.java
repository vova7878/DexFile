package com.v7878.dex.immutable;

public abstract class MemberId {
    abstract TypeId getDeclaringClass();

    abstract String getName();
}
