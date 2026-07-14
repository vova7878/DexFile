package com.v7878.dex.immutable.bytecode.iface;

import com.v7878.dex.ReferenceType;
import com.v7878.dex.immutable.bytecode.InstructionNrcc;
import com.v7878.dex.immutable.bytecode.InstructionNv5cc;

public sealed interface DualReferenceInstruction
        extends SingleReferenceInstruction
        permits InstructionNv5cc, InstructionNrcc {
    Object getReference2();

    default ReferenceType getReferenceType2() {
        return getOpcode().getReferenceType2();
    }
}
