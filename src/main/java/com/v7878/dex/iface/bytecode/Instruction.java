package com.v7878.dex.iface.bytecode;

import com.v7878.dex.Opcode;
import com.v7878.dex.iface.bytecode.formats.Instruction10x;

public sealed interface Instruction permits
        BranchOffsetInstruction, LiteralInstruction, OneRegisterInstruction,
        PayloadInstruction, SingleReferenceInstruction, VariableRegisterInstruction,
        WideLiteralInstruction, Instruction10x {
    Opcode getOpcode();

    default int getUnitCount() {
        assert !getOpcode().isPayload();
        return getOpcode().format().getUnitCount();
    }
}
