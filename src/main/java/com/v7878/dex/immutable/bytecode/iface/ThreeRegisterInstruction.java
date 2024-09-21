package com.v7878.dex.immutable.bytecode.iface;

import com.v7878.dex.immutable.bytecode.Instruction23x;

public sealed interface ThreeRegisterInstruction extends TwoRegisterInstruction
        permits Instruction23x, VariableFiveRegisterInstruction {
    int getRegister3();
}
