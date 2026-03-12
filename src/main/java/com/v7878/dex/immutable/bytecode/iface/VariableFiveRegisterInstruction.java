package com.v7878.dex.immutable.bytecode.iface;

import com.v7878.dex.immutable.bytecode.Instruction35c;
import com.v7878.dex.immutable.bytecode.Instruction45cc;

public sealed interface VariableFiveRegisterInstruction extends
        VariableFourRegisterInstruction
        permits Instruction35c, Instruction45cc {

    int getRegister5();
}
