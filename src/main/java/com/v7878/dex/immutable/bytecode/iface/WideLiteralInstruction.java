package com.v7878.dex.immutable.bytecode.iface;

import com.v7878.dex.immutable.bytecode.Instruction11p;
import com.v7878.dex.immutable.bytecode.Instruction21lh;
import com.v7878.dex.immutable.bytecode.Instruction51l;

public sealed interface WideLiteralInstruction extends InstructionI
        permits Instruction11p, Instruction21lh, Instruction51l {
    long getWideLiteral();
}
