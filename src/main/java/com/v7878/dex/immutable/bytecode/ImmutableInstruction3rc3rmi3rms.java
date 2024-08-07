package com.v7878.dex.immutable.bytecode;

import com.v7878.dex.Opcode;
import com.v7878.dex.base.bytecode.BaseInstruction3rc3rmi3rms;
import com.v7878.dex.iface.bytecode.formats.Instruction3rc3rmi3rms;
import com.v7878.dex.immutable.ImmutableReferenceFactory;
import com.v7878.dex.util.Preconditions;

public class ImmutableInstruction3rc3rmi3rms extends BaseInstruction3rc3rmi3rms implements ImmutableInstruction {
    private final int start_register;
    private final int register_count;
    private final Object reference1;

    protected ImmutableInstruction3rc3rmi3rms(Opcode opcode, int start_register,
                                              int register_count, Object reference1) {
        super(opcode);
        this.start_register = Preconditions.checkShortRegister(start_register);
        this.register_count = Preconditions.checkRegisterRangeCount(register_count);
        this.reference1 = ImmutableReferenceFactory.of(opcode.getReferenceType1(), reference1);
    }

    public static ImmutableInstruction3rc3rmi3rms of(Opcode opcode, int start_register,
                                                     int register_count, Object reference1) {
        return new ImmutableInstruction3rc3rmi3rms(opcode,
                start_register, register_count, reference1);
    }

    public static ImmutableInstruction3rc3rmi3rms of(Instruction3rc3rmi3rms other) {
        if (other instanceof ImmutableInstruction3rc3rmi3rms immutable) return immutable;
        return new ImmutableInstruction3rc3rmi3rms(other.getOpcode(),
                other.getStartRegister(), other.getRegisterCount(), other.getReference1());
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
}
