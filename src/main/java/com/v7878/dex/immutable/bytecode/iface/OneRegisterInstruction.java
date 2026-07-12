package com.v7878.dex.immutable.bytecode.iface;

import com.v7878.dex.immutable.bytecode.InstructionN1c;
import com.v7878.dex.immutable.bytecode.InstructionN1i;
import com.v7878.dex.immutable.bytecode.InstructionN1l;
import com.v7878.dex.immutable.bytecode.InstructionN1p;
import com.v7878.dex.immutable.bytecode.InstructionN1t;
import com.v7878.dex.immutable.bytecode.InstructionN1x;

public sealed interface OneRegisterInstruction extends InstructionI
        permits InstructionN1i, InstructionN1p, InstructionN1x, InstructionN1c,
        InstructionN1l, InstructionN1t, TwoRegisterInstruction {
    int getRegister1();
}
