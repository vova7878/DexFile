package com.v7878.dex.immutable.bytecode;

import com.v7878.dex.Opcode;
import com.v7878.dex.base.bytecode.BaseInstruction10x;
import com.v7878.dex.iface.bytecode.formats.Instruction10x;

public class ImmutableInstruction10x extends BaseInstruction10x implements ImmutableInstruction {
    protected ImmutableInstruction10x(Opcode opcode) {
        super(opcode);
    }

    public static ImmutableInstruction10x of(Opcode opcode) {
        return new ImmutableInstruction10x(opcode);
    }

    public static ImmutableInstruction10x of(Instruction10x other) {
        if (other instanceof ImmutableInstruction10x immutable) return immutable;
        return new ImmutableInstruction10x(other.getOpcode());
    }
}
