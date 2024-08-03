package com.v7878.dex.iface.bytecode;

import com.v7878.dex.iface.bytecode.formats.Instruction10t;
import com.v7878.dex.iface.bytecode.formats.Instruction20t;
import com.v7878.dex.iface.bytecode.formats.Instruction21t;
import com.v7878.dex.iface.bytecode.formats.Instruction22t;
import com.v7878.dex.iface.bytecode.formats.Instruction30t;
import com.v7878.dex.iface.bytecode.formats.Instruction31t;

public sealed interface BranchOffsetInstruction extends Instruction permits
        Instruction10t, Instruction20t, Instruction21t,
        Instruction22t, Instruction30t, Instruction31t {
    int getBranchOffset();
}
