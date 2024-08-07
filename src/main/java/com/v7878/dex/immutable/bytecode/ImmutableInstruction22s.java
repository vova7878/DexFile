package com.v7878.dex.immutable.bytecode;

import com.v7878.dex.Opcode;
import com.v7878.dex.base.bytecode.BaseInstruction22s;
import com.v7878.dex.iface.bytecode.formats.Instruction22s;
import com.v7878.dex.util.Preconditions;

public class ImmutableInstruction22s extends BaseInstruction22s implements ImmutableInstruction {
    private final int register1;
    private final int register2;
    private final int literal;

    protected ImmutableInstruction22s(Opcode opcode, int register1, int register2, int literal) {
        super(opcode);
        this.register1 = Preconditions.checkNibbleRegister(register1);
        this.register2 = Preconditions.checkNibbleRegister(register2);
        this.literal = Preconditions.checkShortLiteral(literal);
    }

    public static ImmutableInstruction22s of(
            Opcode opcode, int register1, int register2, int literal) {
        return new ImmutableInstruction22s(opcode, register1, register2, literal);
    }

    public static ImmutableInstruction22s of(Instruction22s other) {
        if (other instanceof ImmutableInstruction22s immutable) return immutable;
        return new ImmutableInstruction22s(other.getOpcode(),
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
