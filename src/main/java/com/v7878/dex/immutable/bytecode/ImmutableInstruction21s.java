package com.v7878.dex.immutable.bytecode;

import com.v7878.dex.Opcode;
import com.v7878.dex.base.bytecode.BaseInstruction21s;
import com.v7878.dex.iface.bytecode.formats.Instruction21s;
import com.v7878.dex.util.Preconditions;

public class ImmutableInstruction21s extends BaseInstruction21s implements ImmutableInstruction {
    private final int register1;
    private final int literal;

    protected ImmutableInstruction21s(Opcode opcode, int register1, int literal) {
        super(opcode);
        this.register1 = Preconditions.checkByteRegister(register1);
        this.literal = Preconditions.checkShortLiteral(literal);
    }

    public static ImmutableInstruction21s of(Opcode opcode, int register1, int literal) {
        return new ImmutableInstruction21s(opcode, register1, literal);
    }

    public static ImmutableInstruction21s of(Instruction21s other) {
        if (other instanceof ImmutableInstruction21s immutable) return immutable;
        return new ImmutableInstruction21s(other.getOpcode(),
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
