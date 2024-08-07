package com.v7878.dex.iface.bytecode;

import java.util.List;

public interface SwitchPayloadInstruction extends PayloadInstruction {
    List<? extends SwitchElement> getSwitchElements();
}
