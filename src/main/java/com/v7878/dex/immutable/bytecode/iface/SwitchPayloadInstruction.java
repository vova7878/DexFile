package com.v7878.dex.immutable.bytecode.iface;

import com.v7878.dex.immutable.bytecode.SwitchElement;

import java.util.List;

public interface SwitchPayloadInstruction extends PayloadInstruction {
    List<SwitchElement> getSwitchElements();
}
