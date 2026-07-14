package com.v7878.dex.immutable.bytecode.iface;

import com.v7878.dex.immutable.bytecode.InstructionN3x;

public sealed interface ThreeRegisterInstruction extends TwoRegisterInstruction
        permits InstructionN3x, VariableFourRegisterInstruction {
    int getRegister3();
}
