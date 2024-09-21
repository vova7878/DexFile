package com.v7878.dex.immutable.bytecode.iface;

import java.util.List;

public interface ArrayPayloadInstruction extends PayloadInstruction {
    int getElementWidth();

    List<Number> getArrayElements();
}
