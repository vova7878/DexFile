package com.v7878.dex.immutable.bytecode.iface;

import com.v7878.dex.immutable.bytecode.InstructionNv4c;

import java.util.Arrays;

public sealed interface VariableFourRegisterInstruction extends
        ThreeRegisterInstruction, VariableRegisterInstruction
        permits InstructionNv4c, VariableFiveRegisterInstruction {
    int getRegister4();

    default int[] getRegisters() {
        int[] regs = {
                getRegister1(),
                getRegister2(),
                getRegister3(),
                getRegister4()
        };
        return Arrays.copyOf(regs, getRegisterCount());
    }
}
