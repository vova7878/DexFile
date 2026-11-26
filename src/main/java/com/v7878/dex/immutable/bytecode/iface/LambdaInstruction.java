package com.v7878.dex.immutable.bytecode.iface;

import com.v7878.dex.immutable.bytecode.InstructionNv5x;

public sealed interface LambdaInstruction extends InstructionI permits InstructionNv5x {
    // vC is the closure register (the pair vC, vC+1)
    int getClosureRegister();
}
