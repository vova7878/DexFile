package com.v7878.dex.iface;

import java.util.List;

public interface ProtoId extends Comparable<ProtoId> {
    List<? extends TypeId> getParameterTypes();

    String getReturnType();
}
