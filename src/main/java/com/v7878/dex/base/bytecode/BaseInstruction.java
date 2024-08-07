package com.v7878.dex.base.bytecode;

import com.v7878.dex.Opcode;
import com.v7878.dex.iface.bytecode.Instruction;

public class BaseInstruction implements Instruction {
    private final Opcode opcode;

    public BaseInstruction(Opcode opcode) {
        this.opcode = opcode;
    }

    @Override
    public final Opcode getOpcode() {
        return opcode;
    }
}
