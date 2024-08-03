package com.v7878.dex.iface.bytecode;

import com.v7878.dex.iface.bytecode.formats.PackedSwitchPayload;
import com.v7878.dex.iface.bytecode.formats.SparseSwitchPayload;

import java.util.List;

public sealed interface SwitchPayloadInstruction extends PayloadInstruction
        permits PackedSwitchPayload, SparseSwitchPayload {
    List<? extends SwitchElement> getSwitchElements();
}
