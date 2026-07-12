package com.v7878.dex.immutable.bytecode.iface;

import com.v7878.dex.immutable.bytecode.InstructionN1i;
import com.v7878.dex.immutable.bytecode.InstructionN1p;
import com.v7878.dex.immutable.bytecode.InstructionN2i;

public sealed interface LiteralInstruction extends InstructionI
        permits InstructionN1i, InstructionN1p, InstructionN2i {
    int getLiteral();
}
