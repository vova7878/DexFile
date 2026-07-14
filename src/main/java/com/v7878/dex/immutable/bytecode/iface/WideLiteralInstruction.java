package com.v7878.dex.immutable.bytecode.iface;

import com.v7878.dex.immutable.bytecode.InstructionN1l;
import com.v7878.dex.immutable.bytecode.InstructionN1p;

public sealed interface WideLiteralInstruction extends InstructionI
        permits InstructionN1p, InstructionN1l {
    long getWideLiteral();
}
