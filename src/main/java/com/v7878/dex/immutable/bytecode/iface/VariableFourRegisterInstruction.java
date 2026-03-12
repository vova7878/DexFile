package com.v7878.dex.immutable.bytecode.iface;

import com.v7878.dex.immutable.bytecode.Instruction34c;

public sealed interface VariableFourRegisterInstruction extends
        ThreeRegisterInstruction, VariableRegisterInstruction
        permits Instruction34c, VariableFiveRegisterInstruction {
    int getRegister4();
}
