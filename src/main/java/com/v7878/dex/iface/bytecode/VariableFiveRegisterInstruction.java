package com.v7878.dex.iface.bytecode;

import com.v7878.dex.iface.bytecode.formats.Instruction35c35mi35ms;
import com.v7878.dex.iface.bytecode.formats.Instruction45cc;

public sealed interface VariableFiveRegisterInstruction
        extends ThreeRegisterInstruction, VariableRegisterInstruction
        permits Instruction35c35mi35ms, Instruction45cc {
    int getRegister4();

    int getRegister5();
}
