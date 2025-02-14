package com.v7878.dex.immutable.bytecode.iface;

import com.v7878.dex.immutable.bytecode.ArrayPayload;

import java.util.List;

public sealed interface ArrayPayloadInstruction
        extends PayloadInstruction permits ArrayPayload {
    int getElementWidth();

    List<? extends Number> getArrayElements();
}
