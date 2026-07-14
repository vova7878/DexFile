package com.v7878.dex.immutable.bytecode.iface;

import com.v7878.dex.ReferenceType;
import com.v7878.dex.immutable.bytecode.InstructionN1c;
import com.v7878.dex.immutable.bytecode.InstructionN2c;
import com.v7878.dex.immutable.bytecode.InstructionNrc;
import com.v7878.dex.immutable.bytecode.InstructionNv4c;
import com.v7878.dex.immutable.bytecode.InstructionNv5c;
import com.v7878.dex.immutable.bytecode.InstructionRaw0c;

public sealed interface SingleReferenceInstruction extends InstructionI permits
        InstructionN1c, InstructionN2c, InstructionNv4c, InstructionNv5c,
        InstructionNrc, InstructionRaw0c, DualReferenceInstruction {
    Object getReference1();

    default ReferenceType getReferenceType1() {
        return getOpcode().getReferenceType1();
    }
}
