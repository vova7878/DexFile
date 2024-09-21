package com.v7878.dex.immutable.bytecode.iface;

import com.v7878.dex.Opcode;

public interface InstructionI {
    Opcode getOpcode();

    default int getUnitCount() {
        assert !getOpcode().isPayload();
        return getOpcode().format().getUnitCount();
    }
}
