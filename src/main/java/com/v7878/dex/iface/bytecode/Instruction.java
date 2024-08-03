package com.v7878.dex.iface.bytecode;

import com.v7878.dex.iface.bytecode.formats.Instruction10x;

public sealed interface Instruction permits
        BranchOffsetInstruction, LiteralInstruction, OneRegisterInstruction,
        PayloadInstruction, ReferenceInstruction, VariableRegisterInstruction,
        WideLiteralInstruction, Instruction10x {
}
