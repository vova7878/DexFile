package com.v7878.dex.immutable.bytecode;

import com.v7878.dex.Opcode;
import com.v7878.dex.base.bytecode.BaseInstruction11x;
import com.v7878.dex.iface.bytecode.formats.Instruction11x;
import com.v7878.dex.util.Preconditions;

public class ImmutableInstruction11x extends BaseInstruction11x implements ImmutableInstruction {
    private final int register1;

    protected ImmutableInstruction11x(Opcode opcode, int register1) {
        super(opcode);
        this.register1 = Preconditions.checkNibbleRegister(register1);
    }

    public static ImmutableInstruction11x of(Opcode opcode, int register1) {
        return new ImmutableInstruction11x(opcode, register1);
    }

    public static ImmutableInstruction11x of(Instruction11x other) {
        if (other instanceof ImmutableInstruction11x immutable) return immutable;
        return new ImmutableInstruction11x(other.getOpcode(), other.getRegister1());
    }

    @Override
    public int getRegister1() {
        return register1;
    }
}
