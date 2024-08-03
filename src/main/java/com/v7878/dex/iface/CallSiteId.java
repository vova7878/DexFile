package com.v7878.dex.iface;

import com.v7878.dex.iface.value.EncodedValue;

import java.util.List;

public interface CallSiteId {
    MethodHandleId getMethodHandle();

    String getMethodName();

    ProtoId getMethodProto();

    List<? extends EncodedValue> getExtraArguments();
}
