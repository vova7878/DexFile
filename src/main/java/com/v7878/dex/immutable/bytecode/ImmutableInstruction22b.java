package com.v7878.dex.immutable.bytecode;

import com.v7878.dex.Opcode;
import com.v7878.dex.base.bytecode.BaseInstruction22b;
import com.v7878.dex.iface.bytecode.formats.Instruction22b;
import com.v7878.dex.util.Preconditions;

public class ImmutableInstruction22b extends BaseInstruction22b implements ImmutableInstruction {
    private final int register1;
    private final int register2;
    private final int literal;

    protected ImmutableInstruction22b(Opcode opcode, int register1, int register2, int literal) {
        super(opcode);
        this.register1 = Preconditions.checkByteRegister(register1);
        this.register2 = Preconditions.checkByteRegister(register2);
        this.literal = Preconditions.checkByteLiteral(literal);
    }

    public static ImmutableInstruction22b of(
            Opcode opcode, int register1, int register2, int literal) {
        return new ImmutableInstruction22b(opcode, register1, register2, literal);
    }

    public static ImmutableInstruction22b of(Instruction22b other) {
        if (other instanceof ImmutableInstruction22b immutable) return immutable;
        return new ImmutableInstruction22b(other.getOpcode(),
                other.getRegister1(), other.getRegister2(), other.getLiteral());
    }

    @Override
    public int getRegister1() {
        return register1;
    }

    @Override
    public int getRegister2() {
        return register2;
    }

    @Override
    public int getLiteral() {
        return literal;
    }
}
