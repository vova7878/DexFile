package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Format.Format22c22cs;

import com.v7878.dex.Opcode;
import com.v7878.dex.base.bytecode.BaseInstruction22c22cs;
import com.v7878.dex.iface.bytecode.formats.Instruction22c22cs;
import com.v7878.dex.immutable.ImmutableReferenceFactory;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public class ImmutableInstruction22c22cs extends BaseInstruction22c22cs implements ImmutableInstruction {
    private final int register1;
    private final int register2;
    private final Object reference1;

    protected ImmutableInstruction22c22cs(
            Opcode opcode, int register1, int register2, Object reference1) {
        super(Preconditions.checkFormat(opcode, Format22c22cs));
        this.register1 = Preconditions.checkNibbleRegister(register1);
        this.register2 = Preconditions.checkNibbleRegister(register2);
        this.reference1 = ImmutableReferenceFactory.of(opcode.getReferenceType1(), reference1);
    }

    public static ImmutableInstruction22c22cs of(
            Opcode opcode, int register1, int register2, Object reference1) {
        return new ImmutableInstruction22c22cs(opcode, register1, register2, reference1);
    }

    public static ImmutableInstruction22c22cs of(Instruction22c22cs other) {
        if (other instanceof ImmutableInstruction22c22cs immutable) return immutable;
        return new ImmutableInstruction22c22cs(other.getOpcode(),
                other.getRegister1(), other.getRegister2(), other.getReference1());
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
    public Object getReference1() {
        return reference1;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOpcode(), getRegister1(), getRegister2(), getReference1());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Instruction22c22cs other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getRegister1() == other.getRegister1()
                && getRegister2() == other.getRegister2()
                && Objects.equals(getReference1(), other.getReference1());
    }
}
