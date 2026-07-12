package com.v7878.dex.immutable.bytecode.iface;

import com.v7878.dex.immutable.bytecode.InstructionN2c;
import com.v7878.dex.immutable.bytecode.InstructionN2i;
import com.v7878.dex.immutable.bytecode.InstructionN2t;
import com.v7878.dex.immutable.bytecode.InstructionN2x;

public sealed interface TwoRegisterInstruction extends OneRegisterInstruction
        permits InstructionN2x, InstructionN2i, InstructionN2c,
        InstructionN2t, ThreeRegisterInstruction {
    int getRegister2();
}
