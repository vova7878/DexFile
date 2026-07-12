package com.v7878.dex.immutable.bytecode.iface;

import com.v7878.dex.immutable.bytecode.SwitchElement;
import com.v7878.dex.immutable.bytecode.SwitchPayload;

import java.util.NavigableSet;

public sealed interface SwitchPayloadInstruction
        extends PayloadInstruction permits SwitchPayload {
    NavigableSet<SwitchElement> getSwitchElements();
}
