package com.v7878.dex.iface.bytecode;

import com.v7878.dex.Opcode;

public interface Instruction {
    Opcode getOpcode();

    default int getUnitCount() {
        assert !getOpcode().isPayload();
        return getOpcode().format().getUnitCount();
    }
}
