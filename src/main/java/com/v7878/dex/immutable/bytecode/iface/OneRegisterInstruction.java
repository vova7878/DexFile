package com.v7878.dex.immutable.bytecode.iface;

import com.v7878.dex.immutable.bytecode.InstructionN1c;
import com.v7878.dex.immutable.bytecode.InstructionN1i;
import com.v7878.dex.immutable.bytecode.InstructionN1ih;
import com.v7878.dex.immutable.bytecode.InstructionN1l;
import com.v7878.dex.immutable.bytecode.InstructionN1lh;
import com.v7878.dex.immutable.bytecode.InstructionN1p;
import com.v7878.dex.immutable.bytecode.InstructionN1t;
import com.v7878.dex.immutable.bytecode.InstructionN1x;

public sealed interface OneRegisterInstruction extends InstructionI
        permits InstructionN1i, InstructionN1p, InstructionN1x, InstructionN1c,
        InstructionN1ih, InstructionN1lh, InstructionN1t,
        InstructionN1l, TwoRegisterInstruction {
    int getRegister1();
}
