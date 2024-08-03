package com.v7878.dex.iface.bytecode;

import com.v7878.dex.iface.bytecode.formats.Instruction21lh;
import com.v7878.dex.iface.bytecode.formats.Instruction51l;

public sealed interface WideLiteralInstruction extends Instruction
        permits Instruction21lh, Instruction51l {
    long getWideLiteral();
}
