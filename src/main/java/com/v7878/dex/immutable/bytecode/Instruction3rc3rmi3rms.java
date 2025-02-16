package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Format.Format3rc3rmi3rms;

import com.v7878.dex.Opcode;
import com.v7878.dex.ReferenceType;
import com.v7878.dex.immutable.bytecode.iface.RegisterRangeInstruction;
import com.v7878.dex.immutable.bytecode.iface.SingleReferenceInstruction;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public final class Instruction3rc3rmi3rms extends Instruction
        implements RegisterRangeInstruction, SingleReferenceInstruction {
    private final int start_register;
    private final int register_count;
    private final Object reference1;

    private Instruction3rc3rmi3rms(Opcode opcode, int start_register,
                                   int register_count, Object reference1) {
        super(Preconditions.checkFormat(opcode, Format3rc3rmi3rms));
        // TODO: check start_register + register_count not overflows
        this.start_register = Preconditions.checkShortRegister(start_register);
        this.register_count = Preconditions.checkRegisterRangeCount(register_count);
        this.reference1 = ReferenceType.validate(opcode.getReferenceType1(), reference1);
    }

    public static Instruction3rc3rmi3rms of(Opcode opcode, int start_register,
                                            int register_count, Object reference1) {
        return new Instruction3rc3rmi3rms(opcode,
                start_register, register_count, reference1);
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
    public int hashCode() {
        return Objects.hash(getOpcode(), getRegisterCount(), getStartRegister(), getReference1());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Instruction3rc3rmi3rms other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getRegisterCount() == other.getRegisterCount()
                && getStartRegister() == other.getStartRegister()
                && Objects.equals(getReference1(), other.getReference1());
    }
}
