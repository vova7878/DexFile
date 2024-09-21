package com.v7878.dex.immutable.bytecode.iface;

import com.v7878.dex.immutable.bytecode.Instruction10t;
import com.v7878.dex.immutable.bytecode.Instruction20t;
import com.v7878.dex.immutable.bytecode.Instruction21t;
import com.v7878.dex.immutable.bytecode.Instruction22t;
import com.v7878.dex.immutable.bytecode.Instruction30t;
import com.v7878.dex.immutable.bytecode.Instruction31t;

public sealed interface BranchOffsetInstruction extends InstructionI
        permits Instruction10t, Instruction20t, Instruction21t,
        Instruction22t, Instruction30t, Instruction31t {
    int getBranchOffset();
}
