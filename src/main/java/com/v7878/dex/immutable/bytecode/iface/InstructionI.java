package com.v7878.dex.immutable.bytecode.iface;

import com.v7878.dex.Opcode;
import com.v7878.dex.immutable.bytecode.Instruction;

public sealed interface InstructionI permits Instruction, BranchOffsetInstruction,
        IndexInstruction, LiteralInstruction, OneRegisterInstruction, PayloadInstruction,
        RawInstruction, SingleReferenceInstruction, VariableRegisterInstruction,
        VerificationErrorInstruction, WideLiteralInstruction {
    Opcode getOpcode();

    String getName();

    default int getUnitCount() {
        assert !getOpcode().isPayload();
        return getOpcode().getUnitCount();
    }
}
