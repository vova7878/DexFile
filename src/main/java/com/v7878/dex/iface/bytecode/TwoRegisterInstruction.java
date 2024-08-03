package com.v7878.dex.iface.bytecode;

import com.v7878.dex.iface.bytecode.formats.Instruction12x;
import com.v7878.dex.iface.bytecode.formats.Instruction22b;
import com.v7878.dex.iface.bytecode.formats.Instruction22c22cs;
import com.v7878.dex.iface.bytecode.formats.Instruction22s;
import com.v7878.dex.iface.bytecode.formats.Instruction22t;
import com.v7878.dex.iface.bytecode.formats.Instruction22x;
import com.v7878.dex.iface.bytecode.formats.Instruction32x;

public sealed interface TwoRegisterInstruction extends OneRegisterInstruction permits
        ThreeRegisterInstruction, Instruction12x, Instruction22b, Instruction22c22cs,
        Instruction22s, Instruction22t, Instruction22x, Instruction32x {
    int getRegister2();
}
