package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Format.Format31c;

import com.v7878.dex.Opcode;
import com.v7878.dex.base.bytecode.BaseInstruction31c;
import com.v7878.dex.iface.bytecode.formats.Instruction31c;
import com.v7878.dex.immutable.ImmutableReferenceFactory;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public class ImmutableInstruction31c extends BaseInstruction31c implements ImmutableInstruction {
    private final int register1;
    private final Object reference1;

    protected ImmutableInstruction31c(Opcode opcode, int register1, Object reference1) {
        super(Preconditions.checkFormat(opcode, Format31c));
        this.register1 = Preconditions.checkByteRegister(register1);
        this.reference1 = ImmutableReferenceFactory.of(opcode.getReferenceType1(), reference1);
    }

    public static ImmutableInstruction31c of(Opcode opcode, int register1, Object reference1) {
        return new ImmutableInstruction31c(opcode, register1, reference1);
    }

    public static ImmutableInstruction31c of(Instruction31c other) {
        if (other instanceof ImmutableInstruction31c immutable) return immutable;
        return new ImmutableInstruction31c(other.getOpcode(),
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

    @Override
    public int hashCode() {
        return Objects.hash(getOpcode(), getRegister1(), getReference1());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Instruction31c other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getRegister1() == other.getRegister1()
                && Objects.equals(getReference1(), other.getReference1());
    }
}
