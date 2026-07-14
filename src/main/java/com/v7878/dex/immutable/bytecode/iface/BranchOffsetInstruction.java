package com.v7878.dex.immutable.bytecode.iface;

import com.v7878.dex.immutable.bytecode.InstructionN0t;
import com.v7878.dex.immutable.bytecode.InstructionN1t;
import com.v7878.dex.immutable.bytecode.InstructionN2t;

public sealed interface BranchOffsetInstruction extends InstructionI
        permits InstructionN0t, InstructionN1t, InstructionN2t {
    int getBranchOffset();
}
