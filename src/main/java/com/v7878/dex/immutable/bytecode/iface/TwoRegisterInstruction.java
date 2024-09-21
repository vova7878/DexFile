package com.v7878.dex.immutable.bytecode.iface;

import com.v7878.dex.immutable.bytecode.Instruction12x;
import com.v7878.dex.immutable.bytecode.Instruction22b;
import com.v7878.dex.immutable.bytecode.Instruction22c22cs;
import com.v7878.dex.immutable.bytecode.Instruction22s;
import com.v7878.dex.immutable.bytecode.Instruction22t;
import com.v7878.dex.immutable.bytecode.Instruction22x;
import com.v7878.dex.immutable.bytecode.Instruction32x;

public sealed interface TwoRegisterInstruction extends OneRegisterInstruction
        permits Instruction12x, Instruction22b, Instruction22c22cs,
        Instruction22s, Instruction22t, Instruction22x,
        Instruction32x, ThreeRegisterInstruction {
    int getRegister2();
}
