package com.v7878.dex.iface.bytecode;

import com.v7878.dex.iface.bytecode.formats.Instruction45cc;
import com.v7878.dex.iface.bytecode.formats.Instruction4rcc;

public sealed interface DualReferenceInstruction extends ReferenceInstruction
        permits Instruction45cc, Instruction4rcc {
    Object getReference1();
}
