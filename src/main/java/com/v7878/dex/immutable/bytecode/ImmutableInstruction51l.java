package com.v7878.dex.immutable.bytecode;

import com.v7878.dex.Opcode;
import com.v7878.dex.base.bytecode.BaseInstruction51l;
import com.v7878.dex.iface.bytecode.formats.Instruction51l;
import com.v7878.dex.util.Preconditions;

public class ImmutableInstruction51l extends BaseInstruction51l implements ImmutableInstruction {
    private final int register1;
    private final long literal;

    protected ImmutableInstruction51l(Opcode opcode, int register1, long literal) {
        super(opcode);
        this.register1 = Preconditions.checkByteRegister(register1);
        this.literal = literal;
    }

    public static ImmutableInstruction51l of(Opcode opcode, int register1, long literal) {
        return new ImmutableInstruction51l(opcode, register1, literal);
    }

    public static ImmutableInstruction51l of(Instruction51l other) {
        if (other instanceof ImmutableInstruction51l immutable) return immutable;
        return new ImmutableInstruction51l(other.getOpcode(),
                other.getRegister1(), other.getWideLiteral());
    }

    @Override
    public int getRegister1() {
        return register1;
    }

    @Override
    public long getWideLiteral() {
        return literal;
    }
}
