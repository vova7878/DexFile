package com.v7878.dex.iface;

import com.v7878.dex.util.ShortyUtils;

public interface TypeId extends Comparable<TypeId> {
    String getDescriptor();

    default char getShorty() {
        return ShortyUtils.getTypeShorty(this);
    }

    default int getRegisterCount() {
        return ShortyUtils.getRegisterCount(this);
    }
}
