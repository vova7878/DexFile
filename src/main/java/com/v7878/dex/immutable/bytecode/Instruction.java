package com.v7878.dex.immutable.bytecode;

import com.v7878.dex.Opcode;
import com.v7878.dex.immutable.bytecode.iface.InstructionI;

public abstract sealed class Instruction implements InstructionI permits
        ArrayPayload, InstructionN0t, InstructionN0x, InstructionN1i,
        InstructionN1p, InstructionN1x, InstructionN2x, InstructionN1c,
        InstructionN1l, InstructionN1t, InstructionN2i, InstructionN2c,
        InstructionN2t, InstructionN3x, InstructionNv4c, InstructionNv5c,
        InstructionNrc, InstructionNv5cc, InstructionNrcc, InstructionRaw0x,
        InstructionRaw0c, SwitchPayload {
    private final Opcode opcode;

    public Instruction(Opcode opcode) {
        this.opcode = opcode;
    }

    @Override
    public final Opcode getOpcode() {
        return opcode;
    }

    @Override
    public String getName() {
        return opcode.opname();
    }

    @Override
    public abstract String toString();
}
