package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Format.Format11n;

import com.v7878.dex.Opcode;
import com.v7878.dex.immutable.bytecode.iface.LiteralInstruction;
import com.v7878.dex.immutable.bytecode.iface.OneRegisterInstruction;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public class Instruction11n extends Instruction
        implements OneRegisterInstruction, LiteralInstruction {
    private final int register1;
    private final int literal;

    protected Instruction11n(Opcode opcode, int register1, int literal) {
        super(Preconditions.checkFormat(opcode, Format11n));
        this.register1 = Preconditions.checkNibbleRegister(register1);
        this.literal = Preconditions.checkNibbleLiteral(literal);
    }

    public static Instruction11n of(Opcode opcode, int register1, int literal) {
        return new Instruction11n(opcode, register1, literal);
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
        return obj instanceof Instruction11n other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getRegister1() == other.getRegister1()
                && getLiteral() == other.getLiteral();
    }
}
