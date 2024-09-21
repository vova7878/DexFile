package com.v7878.dex.immutable.bytecode.iface;

import com.v7878.dex.immutable.bytecode.PackedSwitchPayload;
import com.v7878.dex.immutable.bytecode.SparseSwitchPayload;
import com.v7878.dex.immutable.bytecode.SwitchElement;

import java.util.List;

public sealed interface SwitchPayloadInstruction extends PayloadInstruction
        permits PackedSwitchPayload, SparseSwitchPayload {
    List<SwitchElement> getSwitchElements();
}
