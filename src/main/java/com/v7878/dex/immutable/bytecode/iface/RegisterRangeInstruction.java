package com.v7878.dex.immutable.bytecode.iface;

import com.v7878.dex.immutable.bytecode.Instruction3rc3rmi3rms;
import com.v7878.dex.immutable.bytecode.Instruction4rcc;

public sealed interface RegisterRangeInstruction
        extends VariableRegisterInstruction
        permits Instruction3rc3rmi3rms, Instruction4rcc {
    int getStartRegister();
}
