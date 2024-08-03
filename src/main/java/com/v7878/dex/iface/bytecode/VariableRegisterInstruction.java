package com.v7878.dex.iface.bytecode;

public sealed interface VariableRegisterInstruction extends Instruction
        permits RegisterRangeInstruction, VariableFiveRegisterInstruction {
    int getRegisterCount();
}
