package com.v7878.dex.iface.bytecode;

import com.v7878.dex.iface.bytecode.formats.Instruction3rc3rmi3rms;
import com.v7878.dex.iface.bytecode.formats.Instruction4rcc;

public sealed interface RegisterRangeInstruction extends VariableRegisterInstruction
        permits Instruction3rc3rmi3rms, Instruction4rcc {
    int getStartRegister();
}
