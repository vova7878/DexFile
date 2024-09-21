package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Format.Format51l;

import com.v7878.dex.Opcode;
import com.v7878.dex.immutable.bytecode.iface.OneRegisterInstruction;
import com.v7878.dex.immutable.bytecode.iface.WideLiteralInstruction;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public final class Instruction51l extends Instruction
        implements OneRegisterInstruction, WideLiteralInstruction {
    private final int register1;
    private final long literal;

    private Instruction51l(Opcode opcode, int register1, long literal) {
        super(Preconditions.checkFormat(opcode, Format51l));
        this.register1 = Preconditions.checkByteRegister(register1);
        this.literal = literal;
    }

    public static Instruction51l of(Opcode opcode, int register1, long literal) {
        return new Instruction51l(opcode, register1, literal);
    }

    @Override
    public int getRegister1() {
        return register1;
    }

    @Override
    public long getWideLiteral() {
        return literal;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOpcode(), getRegister1(), getWideLiteral());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Instruction51l other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getRegister1() == other.getRegister1()
                && getWideLiteral() == other.getWideLiteral();
    }
}
