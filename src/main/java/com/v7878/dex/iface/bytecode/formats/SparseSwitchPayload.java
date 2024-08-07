package com.v7878.dex.iface.bytecode.formats;

import com.v7878.dex.iface.bytecode.SwitchPayloadInstruction;

public interface SparseSwitchPayload extends SwitchPayloadInstruction {
    @Override
    default int getUnitCount() {
        return getSwitchElements().size() * 4 + 2;
    }
}
