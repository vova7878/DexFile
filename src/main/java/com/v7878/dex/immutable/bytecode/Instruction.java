package com.v7878.dex.immutable.bytecode;

import com.v7878.dex.Opcode;
import com.v7878.dex.immutable.bytecode.iface.InstructionI;

public abstract sealed class Instruction implements InstructionI permits
        ArrayPayload, Instruction10t, Instruction10x, Instruction11n, Instruction11x,
        Instruction12x, Instruction20t, Instruction21c, Instruction21ih, Instruction21lh,
        Instruction21s, Instruction21t, Instruction22b, Instruction22c22cs, Instruction22s,
        Instruction22t, Instruction22x, Instruction23x, Instruction30t, Instruction31c,
        Instruction31i, Instruction31t, Instruction32x, Instruction35c35mi35ms,
        Instruction3rc3rmi3rms, Instruction45cc, Instruction4rcc, Instruction51l,
        InstructionRaw, PackedSwitchPayload, SparseSwitchPayload {
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
