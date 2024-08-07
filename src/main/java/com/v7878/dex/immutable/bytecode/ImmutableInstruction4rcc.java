package com.v7878.dex.immutable.bytecode;

import com.v7878.dex.Opcode;
import com.v7878.dex.base.bytecode.BaseInstruction4rcc;
import com.v7878.dex.iface.bytecode.formats.Instruction4rcc;
import com.v7878.dex.immutable.ImmutableReferenceFactory;
import com.v7878.dex.util.Preconditions;

public class ImmutableInstruction4rcc extends BaseInstruction4rcc implements ImmutableInstruction {
    private final int start_register;
    private final int register_count;
    private final Object reference1;
    private final Object reference2;

    protected ImmutableInstruction4rcc(Opcode opcode, int start_register, int register_count,
                                       Object reference1, Object reference2) {
        super(opcode);
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
}
