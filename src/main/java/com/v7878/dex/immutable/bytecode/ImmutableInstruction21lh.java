package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Format.Format21lh;

import com.v7878.dex.Opcode;
import com.v7878.dex.base.bytecode.BaseInstruction21lh;
import com.v7878.dex.iface.bytecode.formats.Instruction21lh;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public class ImmutableInstruction21lh extends BaseInstruction21lh implements ImmutableInstruction {
    private final int register1;
    private final long literal;

    protected ImmutableInstruction21lh(Opcode opcode, int register1, long literal) {
        super(Preconditions.checkFormat(opcode, Format21lh));
        this.register1 = Preconditions.checkByteRegister(register1);
        this.literal = Preconditions.checkLongHatLiteral(literal);
    }

    public static ImmutableInstruction21lh of(Opcode opcode, int register1, long literal) {
        return new ImmutableInstruction21lh(opcode, register1, literal);
    }

    public static ImmutableInstruction21lh of(Instruction21lh other) {
        if (other instanceof ImmutableInstruction21lh immutable) return immutable;
        return new ImmutableInstruction21lh(other.getOpcode(),
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
        return obj instanceof Instruction21lh other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getRegister1() == other.getRegister1()
                && getWideLiteral() == other.getWideLiteral();
    }
}
