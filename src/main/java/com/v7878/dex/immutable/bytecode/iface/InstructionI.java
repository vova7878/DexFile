package com.v7878.dex.immutable.bytecode.iface;

import com.v7878.dex.Opcode;
import com.v7878.dex.immutable.bytecode.Instruction;

public sealed interface InstructionI permits Instruction, BranchOffsetInstruction,
        LiteralInstruction, OneRegisterInstruction, PayloadInstruction,
        SingleReferenceInstruction, VariableRegisterInstruction, WideLiteralInstruction {
    Opcode getOpcode();

    default int getUnitCount() {
        assert !getOpcode().isPayload();
        return getOpcode().format().getUnitCount();
    }
}
