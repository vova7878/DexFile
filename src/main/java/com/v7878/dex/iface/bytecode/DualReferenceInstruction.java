package com.v7878.dex.iface.bytecode;

import com.v7878.dex.iface.bytecode.formats.Instruction45cc;
import com.v7878.dex.iface.bytecode.formats.Instruction4rcc;

public sealed interface DualReferenceInstruction extends SingleReferenceInstruction
        permits Instruction45cc, Instruction4rcc {
    Object getReference2();
}
