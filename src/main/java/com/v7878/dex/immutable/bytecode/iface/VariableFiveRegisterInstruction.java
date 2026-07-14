package com.v7878.dex.immutable.bytecode.iface;

import com.v7878.dex.immutable.bytecode.InstructionNv5c;
import com.v7878.dex.immutable.bytecode.InstructionNv5cc;

public sealed interface VariableFiveRegisterInstruction extends
        VariableFourRegisterInstruction permits InstructionNv5c, InstructionNv5cc {

    int getRegister5();
}
