package com.v7878.dex.immutable.bytecode.iface;

import com.v7878.dex.immutable.bytecode.Instruction3rc;
import com.v7878.dex.immutable.bytecode.Instruction4rcc;

public sealed interface RegisterRangeInstruction
        extends VariableRegisterInstruction
        permits Instruction3rc, Instruction4rcc {
    int getStartRegister();
}
