package com.v7878.dex.immutable.bytecode.iface;

public interface VariableFiveRegisterInstruction extends
        ThreeRegisterInstruction, VariableRegisterInstruction {
    int getRegister4();

    int getRegister5();
}
