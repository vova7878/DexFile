package com.v7878.dex.iface;

import java.util.List;

public non-sealed interface MethodId extends MemberId, Comparable<MethodId> {
    List<? extends TypeId> getParameterTypes();

    String getReturnType();
}
