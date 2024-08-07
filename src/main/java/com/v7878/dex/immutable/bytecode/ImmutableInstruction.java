package com.v7878.dex.immutable.bytecode;

import com.v7878.dex.iface.bytecode.Instruction;

public interface ImmutableInstruction extends Instruction {
    static ImmutableInstruction of(Instruction other) {
        throw new UnsupportedOperationException("TODO");
    }
}
