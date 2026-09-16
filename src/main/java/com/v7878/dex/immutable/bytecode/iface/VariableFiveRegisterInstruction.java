package com.v7878.dex.immutable.bytecode.iface;

import com.v7878.dex.immutable.bytecode.InstructionNv5c;
import com.v7878.dex.immutable.bytecode.InstructionNv5cc;

import java.util.Arrays;

public sealed interface VariableFiveRegisterInstruction extends
        VariableFourRegisterInstruction permits InstructionNv5c, InstructionNv5cc {

    int getRegister5();

    default int[] getRegisters() {
        int[] regs = {
                getRegister1(),
                getRegister2(),
                getRegister3(),
                getRegister4(),
                getRegister5()
        };
        return Arrays.copyOf(regs, getRegisterCount());
    }
}
