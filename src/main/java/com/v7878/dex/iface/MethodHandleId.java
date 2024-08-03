package com.v7878.dex.iface;

import com.v7878.dex.MethodHandleType;

public interface MethodHandleId extends Comparable<MethodHandleId> {
    MethodHandleType getMethodHandleType();

    MemberId getMember();
}
