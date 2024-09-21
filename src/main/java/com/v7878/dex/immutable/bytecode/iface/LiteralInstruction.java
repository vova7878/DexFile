package com.v7878.dex.immutable.bytecode.iface;

import com.v7878.dex.immutable.bytecode.Instruction11n;
import com.v7878.dex.immutable.bytecode.Instruction21ih;
import com.v7878.dex.immutable.bytecode.Instruction21s;
import com.v7878.dex.immutable.bytecode.Instruction22b;
import com.v7878.dex.immutable.bytecode.Instruction22s;
import com.v7878.dex.immutable.bytecode.Instruction31i;

public sealed interface LiteralInstruction extends InstructionI
        permits Instruction11n, Instruction21ih, Instruction21s,
        Instruction22b, Instruction22s, Instruction31i {
    int getLiteral();
}
