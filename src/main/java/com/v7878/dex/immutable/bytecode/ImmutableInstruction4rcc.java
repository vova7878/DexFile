package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Format.Format4rcc;

import com.v7878.dex.Opcode;
import com.v7878.dex.base.bytecode.BaseInstruction4rcc;
import com.v7878.dex.iface.bytecode.formats.Instruction4rcc;
import com.v7878.dex.immutable.ImmutableReferenceFactory;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public class ImmutableInstruction4rcc extends BaseInstruction4rcc implements ImmutableInstruction {
    private final int start_register;
    private final int register_count;
    private final Object reference1;
    private final Object reference2;

    protected ImmutableInstruction4rcc(Opcode opcode, int start_register, int register_count,
                                       Object reference1, Object reference2) {
        super(Preconditions.checkFormat(opcode, Format4rcc));
        this.start_register = Preconditions.checkShortRegister(start_register);
        this.register_count = Preconditions.checkRegisterRangeCount(register_count);
        this.reference1 = ImmutableReferenceFactory.of(opcode.getReferenceType1(), reference1);
        this.reference2 = ImmutableReferenceFactory.of(opcode.getReferenceType2(), reference2);
    }

    public static ImmutableInstruction4rcc of(Opcode opcode, int start_register, int register_count,
                                              Object reference1, Object reference2) {
        return new ImmutableInstruction4rcc(opcode,
                start_register, register_count, reference1, reference2);
    }

    public static ImmutableInstruction4rcc of(Instruction4rcc other) {
        if (other instanceof ImmutableInstruction4rcc immutable) return immutable;
        return new ImmutableInstruction4rcc(other.getOpcode(), other.getStartRegister(),
                other.getRegisterCount(), other.getReference1(), other.getReference2());
    }

    @Override
    public int getStartRegister() {
        return start_register;
    }

    @Override
    public int getRegisterCount() {
        return register_count;
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
