package com.v7878.dex.immutable.bytecode.iface;

import com.v7878.dex.ReferenceType;
import com.v7878.dex.immutable.bytecode.Instruction21c;
import com.v7878.dex.immutable.bytecode.Instruction22c;
import com.v7878.dex.immutable.bytecode.Instruction31c;
import com.v7878.dex.immutable.bytecode.Instruction34c;
import com.v7878.dex.immutable.bytecode.Instruction35c;
import com.v7878.dex.immutable.bytecode.Instruction3rc;
import com.v7878.dex.immutable.bytecode.Instruction41c;
import com.v7878.dex.immutable.bytecode.Instruction52c;
import com.v7878.dex.immutable.bytecode.Instruction5rc;

public sealed interface SingleReferenceInstruction extends InstructionI permits Instruction21c,
        Instruction22c, Instruction31c, Instruction34c, Instruction35c, Instruction3rc,
        Instruction41c, Instruction52c, Instruction5rc, DualReferenceInstruction {
    Object getReference1();

    default ReferenceType getReferenceType1() {
        return getOpcode().getReferenceType1();
    }
}
