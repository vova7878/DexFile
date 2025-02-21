package com.v7878.dex.util;

import com.v7878.dex.immutable.bytecode.Instruction;
import com.v7878.dex.immutable.bytecode.iface.VariableRegisterInstruction;

import java.util.List;

public class CodeUtils {
    public static int getOutputRegisterCount(List<Instruction> insns) {
        int out_regs = 0;
        for (var instruction : insns) {
            switch (instruction.getOpcode()) {
                case INVOKE_VIRTUAL, INVOKE_SUPER, INVOKE_DIRECT,
                     INVOKE_STATIC, INVOKE_INTERFACE,

                     INVOKE_VIRTUAL_RANGE, INVOKE_SUPER_RANGE, INVOKE_DIRECT_RANGE,
                     INVOKE_STATIC_RANGE, INVOKE_INTERFACE_RANGE,

                     INVOKE_POLYMORPHIC, INVOKE_POLYMORPHIC_RANGE,
                     INVOKE_CUSTOM, INVOKE_CUSTOM_RANGE,

                     EXECUTE_INLINE, EXECUTE_INLINE_RANGE,

                     INVOKE_DIRECT_EMPTY, INVOKE_OBJECT_INIT_RANGE,

                     INVOKE_VIRTUAL_QUICK, INVOKE_VIRTUAL_QUICK_RANGE,
                     INVOKE_SUPER_QUICK, INVOKE_SUPER_QUICK_RANGE ->
                        out_regs = Math.max(out_regs, ((VariableRegisterInstruction) instruction).getRegisterCount());
            }
        }
        return out_regs;
    }
}
