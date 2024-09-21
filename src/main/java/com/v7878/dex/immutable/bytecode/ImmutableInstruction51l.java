package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Format.Format51l;

import com.v7878.dex.Opcode;
import com.v7878.dex.base.bytecode.BaseInstruction51l;
import com.v7878.dex.iface.bytecode.formats.Instruction51l;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public class ImmutableInstruction51l extends BaseInstruction51l implements ImmutableInstruction {
    private final int register1;
    private final long literal;

    protected ImmutableInstruction51l(Opcode opcode, int register1, long literal) {
        super(Preconditions.checkFormat(opcode, Format51l));
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
