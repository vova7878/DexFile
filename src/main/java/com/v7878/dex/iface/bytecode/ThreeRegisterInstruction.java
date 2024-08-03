package com.v7878.dex.iface.bytecode;

import com.v7878.dex.iface.bytecode.formats.Instruction23x;

public sealed interface ThreeRegisterInstruction extends TwoRegisterInstruction
        permits VariableFiveRegisterInstruction, Instruction23x {
    int getRegister3();
}
