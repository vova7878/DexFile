package com.v7878.dex.immutable.bytecode.iface;

import com.v7878.dex.immutable.bytecode.Instruction;
import com.v7878.dex.immutable.bytecode.InstructionRaw0c;
import com.v7878.dex.immutable.bytecode.InstructionRaw0x;

public sealed interface RawInstruction<I extends Instruction & RawInstruction<I>>
        extends InstructionI permits InstructionRaw0c, InstructionRaw0x {
    I wrapper();

    I raw();
}
