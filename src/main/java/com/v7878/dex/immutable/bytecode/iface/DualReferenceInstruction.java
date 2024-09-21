package com.v7878.dex.immutable.bytecode.iface;

import com.v7878.dex.immutable.bytecode.Instruction45cc;
import com.v7878.dex.immutable.bytecode.Instruction4rcc;

public sealed interface DualReferenceInstruction
        extends SingleReferenceInstruction
        permits Instruction45cc, Instruction4rcc {
    Object getReference2();
}
