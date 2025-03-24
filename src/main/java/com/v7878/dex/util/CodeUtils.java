package com.v7878.dex.util;

import com.v7878.dex.immutable.bytecode.Instruction;
import com.v7878.dex.immutable.bytecode.iface.VariableRegisterInstruction;

import java.util.List;

public class CodeUtils {
    public static int countOutputRegisters(List<Instruction> insns) {
        int out_regs = 0;
        for (var instruction : insns) {
            if (instruction.getOpcode().isInvoke()) {
                out_regs = Math.max(out_regs, ((VariableRegisterInstruction) instruction).getRegisterCount());
            }
        }
        return out_regs;
    }
}
