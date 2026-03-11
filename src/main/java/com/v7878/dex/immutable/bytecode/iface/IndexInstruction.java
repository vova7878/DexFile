package com.v7878.dex.immutable.bytecode.iface;

import com.v7878.dex.immutable.bytecode.Instruction11p;

public sealed interface IndexInstruction extends InstructionI permits Instruction11p {
    int getIndex();
}
