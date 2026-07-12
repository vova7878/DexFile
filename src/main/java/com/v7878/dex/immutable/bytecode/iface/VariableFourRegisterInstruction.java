package com.v7878.dex.immutable.bytecode.iface;

import com.v7878.dex.immutable.bytecode.InstructionNv4c;

public sealed interface VariableFourRegisterInstruction extends
        ThreeRegisterInstruction, VariableRegisterInstruction
        permits InstructionNv4c, VariableFiveRegisterInstruction {
    int getRegister4();
}
