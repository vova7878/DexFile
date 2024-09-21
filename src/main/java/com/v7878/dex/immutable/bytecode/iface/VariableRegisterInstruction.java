package com.v7878.dex.immutable.bytecode.iface;

public sealed interface VariableRegisterInstruction extends InstructionI
        permits RegisterRangeInstruction, VariableFiveRegisterInstruction {
    int getRegisterCount();
}
