package com.v7878.dex.immutable.bytecode;

import com.v7878.dex.Opcode;
import com.v7878.dex.immutable.bytecode.iface.InstructionI;

public class Instruction implements InstructionI {
    private final Opcode opcode;

    public Instruction(Opcode opcode) {
        this.opcode = opcode;
    }

    @Override
    public final Opcode getOpcode() {
        return opcode;
    }
}
