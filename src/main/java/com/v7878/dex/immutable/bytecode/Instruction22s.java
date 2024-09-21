package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Format.Format22s;

import com.v7878.dex.Opcode;
import com.v7878.dex.immutable.bytecode.iface.LiteralInstruction;
import com.v7878.dex.immutable.bytecode.iface.TwoRegisterInstruction;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public final class Instruction22s extends Instruction
        implements TwoRegisterInstruction, LiteralInstruction {
    private final int register1;
    private final int register2;
    private final int literal;

    private Instruction22s(Opcode opcode, int register1, int register2, int literal) {
        super(Preconditions.checkFormat(opcode, Format22s));
        this.register1 = Preconditions.checkNibbleRegister(register1);
        this.register2 = Preconditions.checkNibbleRegister(register2);
        this.literal = Preconditions.checkShortLiteral(literal);
    }

    public static Instruction22s of(
            Opcode opcode, int register1, int register2, int literal) {
        return new Instruction22s(opcode, register1, register2, literal);
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

    @Override
    public int hashCode() {
        return Objects.hash(getOpcode(), getRegister1(), getRegister2(), getLiteral());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Instruction22s other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getRegister1() == other.getRegister1()
                && getRegister2() == other.getRegister2()
                && getLiteral() == other.getLiteral();
    }
}
