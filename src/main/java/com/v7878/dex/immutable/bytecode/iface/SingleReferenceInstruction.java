package com.v7878.dex.immutable.bytecode.iface;

import com.v7878.dex.immutable.bytecode.Instruction21c;
import com.v7878.dex.immutable.bytecode.Instruction22c22cs;
import com.v7878.dex.immutable.bytecode.Instruction31c;
import com.v7878.dex.immutable.bytecode.Instruction35c35mi35ms;
import com.v7878.dex.immutable.bytecode.Instruction3rc3rmi3rms;

public sealed interface SingleReferenceInstruction extends InstructionI
        permits Instruction21c, Instruction22c22cs, Instruction31c,
        Instruction35c35mi35ms, Instruction3rc3rmi3rms, DualReferenceInstruction {
    Object getReference1();
}
