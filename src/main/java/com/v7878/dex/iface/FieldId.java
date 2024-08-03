package com.v7878.dex.iface;

public non-sealed interface FieldId extends MemberId, Comparable<FieldId> {
    TypeId getType();
}
