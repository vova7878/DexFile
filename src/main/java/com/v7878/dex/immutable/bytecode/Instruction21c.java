package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Format.Format21c;

import com.v7878.dex.Opcode;
import com.v7878.dex.ReferenceType;
import com.v7878.dex.immutable.bytecode.iface.OneRegisterInstruction;
import com.v7878.dex.immutable.bytecode.iface.SingleReferenceInstruction;
import com.v7878.dex.util.Formatter;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public final class Instruction21c extends Instruction
        implements OneRegisterInstruction, SingleReferenceInstruction {
    private final int register1;
    private final Object reference1;

    private Instruction21c(Opcode opcode, int register1, Object reference1) {
        super(Preconditions.checkFormat(opcode, Format21c));
        this.register1 = Preconditions.checkByteRegister(register1);
        this.reference1 = ReferenceType.validate(getReferenceType1(), reference1);
    }

    public static Instruction21c of(Opcode opcode, int register1, Object reference1) {
        return new Instruction21c(opcode, register1, reference1);
    }

    @Override
    public int getRegister1() {
        return register1;
    }

    @Override
    public Object getReference1() {
        return reference1;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOpcode(), getRegister1(), getReference1());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Instruction21c other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getRegister1() == other.getRegister1()
                && Objects.equals(getReference1(), other.getReference1());
    }

    @Override
    public String toString() {
        return getName() + " " + Formatter.register(register1)
                + ", " + ReferenceType.describe(getReferenceType1(), reference1);
    }
}
