package com.v7878.dex.immutable.bytecode.iface;

import com.v7878.dex.VerificationError;
import com.v7878.dex.immutable.bytecode.InstructionN0ci;

public sealed interface VerificationErrorInstruction
        extends InstructionI permits InstructionN0ci {
    VerificationError getError();
}
