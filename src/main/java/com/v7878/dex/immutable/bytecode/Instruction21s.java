package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Format.Format21s;

import com.v7878.dex.Opcode;
import com.v7878.dex.immutable.bytecode.iface.LiteralInstruction;
import com.v7878.dex.immutable.bytecode.iface.OneRegisterInstruction;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public class Instruction21s extends Instruction
        implements OneRegisterInstruction, LiteralInstruction {
    private final int register1;
    private final int literal;

    protected Instruction21s(Opcode opcode, int register1, int literal) {
        super(Preconditions.checkFormat(opcode, Format21s));
        this.register1 = Preconditions.checkByteRegister(register1);
        this.literal = Preconditions.checkShortLiteral(literal);
    }

    public static Instruction21s of(Opcode opcode, int register1, int literal) {
        return new Instruction21s(opcode, register1, literal);
    }

    @Override
    public int getRegister1() {
        return register1;
    }

    @Override
    public int getLiteral() {
        return literal;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOpcode(), getRegister1(), getLiteral());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Instruction21s other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getRegister1() == other.getRegister1()
                && getLiteral() == other.getLiteral();
    }
}
