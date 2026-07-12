package com.v7878.dex.immutable.bytecode.iface;

import com.v7878.dex.immutable.bytecode.InstructionNrc;
import com.v7878.dex.immutable.bytecode.InstructionNrcc;

public sealed interface RegisterRangeInstruction
        extends VariableRegisterInstruction
        permits InstructionNrc, InstructionNrcc {
    int getStartRegister();
}
