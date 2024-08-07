package com.v7878.dex.iface.bytecode.formats;

import com.v7878.dex.iface.bytecode.SwitchPayloadInstruction;

public interface PackedSwitchPayload extends SwitchPayloadInstruction {
    @Override
    default int getUnitCount() {
        return getSwitchElements().size() * 2 + 4;
    }
}
