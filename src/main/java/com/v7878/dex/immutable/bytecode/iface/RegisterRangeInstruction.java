package com.v7878.dex.immutable.bytecode.iface;

import com.v7878.dex.immutable.bytecode.InstructionNrc;
import com.v7878.dex.immutable.bytecode.InstructionNrcc;

public sealed interface RegisterRangeInstruction
        extends VariableRegisterInstruction
        permits InstructionNrc, InstructionNrcc {
    int getStartRegister();

    default int[] getRegisters() {
        var regs = new int[getRegisterCount()];
        var start = getStartRegister();
        for (int i = 0; i < regs.length; i++) {
            regs[i] = start + i;
        }
        return regs;
    }
}
