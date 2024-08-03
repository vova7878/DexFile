package com.v7878.dex.iface.bytecode;

import com.v7878.dex.iface.bytecode.formats.Instruction11n;
import com.v7878.dex.iface.bytecode.formats.Instruction21ih;
import com.v7878.dex.iface.bytecode.formats.Instruction21s;
import com.v7878.dex.iface.bytecode.formats.Instruction22b;
import com.v7878.dex.iface.bytecode.formats.Instruction22s;
import com.v7878.dex.iface.bytecode.formats.Instruction31i;

public sealed interface LiteralInstruction extends Instruction permits
        Instruction11n, Instruction21ih, Instruction21s,
        Instruction22b, Instruction22s, Instruction31i {
    int getLiteral();
}
