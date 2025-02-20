package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Format.Format4rcc;

import com.v7878.dex.Opcode;
import com.v7878.dex.ReferenceType;
import com.v7878.dex.immutable.bytecode.iface.DualReferenceInstruction;
import com.v7878.dex.immutable.bytecode.iface.RegisterRangeInstruction;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public final class Instruction4rcc extends Instruction
        implements RegisterRangeInstruction, DualReferenceInstruction {
    private final int register_count;
    private final int start_register;
    private final Object reference1;
    private final Object reference2;

    private Instruction4rcc(Opcode opcode, int register_count, int start_register,
                            Object reference1, Object reference2) {
        super(Preconditions.checkFormat(opcode, Format4rcc));
        // TODO: check start_register + register_count not overflows
        this.register_count = Preconditions.checkRegisterRangeCount(register_count);
        this.start_register = Preconditions.checkShortRegister(start_register);
        this.reference1 = ReferenceType.validate(getReferenceType1(), reference1);
        this.reference2 = ReferenceType.validate(getReferenceType2(), reference2);
    }

    public static Instruction4rcc of(Opcode opcode, int register_count, int start_register,
                                     Object reference1, Object reference2) {
        return new Instruction4rcc(opcode,
                register_count, start_register, reference1, reference2);
    }

    @Override
    public int getRegisterCount() {
        return register_count;
    }

    @Override
    public int getStartRegister() {
        return start_register;
    }

    @Override
    public Object getReference1() {
        return reference1;
    }

    @Override
    public Object getReference2() {
        return reference2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOpcode(), getRegisterCount(),
                getStartRegister(), getReference1(), getReference2());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Instruction4rcc other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getRegisterCount() == other.getRegisterCount()
                && getStartRegister() == other.getStartRegister()
                && Objects.equals(getReference1(), other.getReference1())
                && Objects.equals(getReference2(), other.getReference2());
    }
}
