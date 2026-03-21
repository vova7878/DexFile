package com.v7878.dex.immutable.bytecode.iface;

import com.v7878.dex.immutable.bytecode.Instruction3rc;
import com.v7878.dex.immutable.bytecode.Instruction4rcc;
import com.v7878.dex.immutable.bytecode.Instruction5rc;

public sealed interface RegisterRangeInstruction
        extends VariableRegisterInstruction
        permits Instruction3rc, Instruction4rcc, Instruction5rc {
    int getStartRegister();
}
