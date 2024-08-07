package com.v7878.dex.iface;

import java.util.List;

public interface MethodId extends MemberId, Comparable<MethodId> {
    List<? extends TypeId> getParameterTypes();

    TypeId getReturnType();
}
