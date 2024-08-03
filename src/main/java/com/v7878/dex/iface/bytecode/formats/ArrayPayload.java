package com.v7878.dex.iface.bytecode.formats;

import com.v7878.dex.iface.bytecode.PayloadInstruction;

import java.util.List;

public non-sealed interface ArrayPayload extends PayloadInstruction {
    int getElementWidth();

    List<Number> getArrayElements();
}
