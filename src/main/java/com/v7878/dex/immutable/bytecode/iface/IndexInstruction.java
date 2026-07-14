package com.v7878.dex.immutable.bytecode.iface;

import com.v7878.dex.immutable.bytecode.InstructionN1p;

public sealed interface IndexInstruction extends InstructionI permits InstructionN1p {
    int getIndex();
}
