package com.v7878.dex.immutable.bytecode.iface;

import com.v7878.dex.immutable.bytecode.Instruction35c35mi35ms;
import com.v7878.dex.immutable.bytecode.Instruction45cc;

public sealed interface VariableFiveRegisterInstruction extends
        ThreeRegisterInstruction, VariableRegisterInstruction
        permits Instruction35c35mi35ms, Instruction45cc {
    int getRegister4();

    int getRegister5();
}
