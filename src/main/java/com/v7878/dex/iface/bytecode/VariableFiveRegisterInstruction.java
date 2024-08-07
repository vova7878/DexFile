package com.v7878.dex.iface.bytecode;

public interface VariableFiveRegisterInstruction extends
        ThreeRegisterInstruction, VariableRegisterInstruction {
    int getRegister4();

    int getRegister5();
}
