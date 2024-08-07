package com.v7878.dex.iface;

public interface FieldId extends MemberId, Comparable<FieldId> {
    TypeId getType();
}
