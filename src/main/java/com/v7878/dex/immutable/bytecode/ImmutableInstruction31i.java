package com.v7878.dex.immutable.bytecode;

import com.v7878.dex.Opcode;
import com.v7878.dex.base.bytecode.BaseInstruction31i;
import com.v7878.dex.iface.bytecode.formats.Instruction31i;
import com.v7878.dex.util.Preconditions;

public class ImmutableInstruction31i extends BaseInstruction31i implements ImmutableInstruction {
    private final int register1;
    private final int literal;

    protected ImmutableInstruction31i(Opcode opcode, int register1, int literal) {
        super(opcode);
        this.register1 = Preconditions.checkByteRegister(register1);
        this.literal = literal;
    }

    public static ImmutableInstruction31i of(Opcode opcode, int register1, int literal) {
        return new ImmutableInstruction31i(opcode, register1, literal);
    }

    public static ImmutableInstruction31i of(Instruction31i other) {
        if (other instanceof ImmutableInstruction31i immutable) return immutable;
        return new ImmutableInstruction31i(other.getOpcode(),
                other.getRegister1(), other.getLiteral());
    }

    @Override
    public int getRegister1() {
        return register1;
    }

    @Override
    public int getLiteral() {
        return literal;
    }
}
