package com.v7878.dex.immutable.bytecode;

import com.v7878.dex.Opcode;
import com.v7878.dex.base.bytecode.BaseInstruction21c;
import com.v7878.dex.iface.bytecode.formats.Instruction21c;
import com.v7878.dex.immutable.ImmutableReferenceFactory;
import com.v7878.dex.util.Preconditions;

public class ImmutableInstruction21c extends BaseInstruction21c implements ImmutableInstruction {
    private final int register1;
    private final Object reference1;

    protected ImmutableInstruction21c(Opcode opcode, int register1, Object reference1) {
        super(opcode);
        this.register1 = Preconditions.checkByteRegister(register1);
        this.reference1 = ImmutableReferenceFactory.of(opcode.getReferenceType1(), reference1);
    }

    public static ImmutableInstruction21c of(Opcode opcode, int register1, Object reference1) {
        return new ImmutableInstruction21c(opcode, register1, reference1);
    }

    public static ImmutableInstruction21c of(Instruction21c other) {
        if (other instanceof ImmutableInstruction21c immutable) return immutable;
        return new ImmutableInstruction21c(other.getOpcode(),
                other.getRegister1(), other.getReference1());
    }

    @Override
    public int getRegister1() {
        return register1;
    }

    @Override
    public Object getReference1() {
        return reference1;
    }
}
