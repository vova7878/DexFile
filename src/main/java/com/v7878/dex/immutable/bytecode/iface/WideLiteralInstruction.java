package com.v7878.dex.immutable.bytecode.iface;

import com.v7878.dex.immutable.bytecode.Instruction21lh;
import com.v7878.dex.immutable.bytecode.Instruction51l;

public sealed interface WideLiteralInstruction extends InstructionI
        permits Instruction21lh, Instruction51l {
    long getWideLiteral();
}
