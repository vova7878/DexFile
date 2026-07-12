package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Format.Format21c;
import static com.v7878.dex.Format.Format31c;
import static com.v7878.dex.Format.Format41c;
import static com.v7878.dex.util.Checks.shouldNotReachHere;

import com.v7878.dex.Opcode;
import com.v7878.dex.ReferenceType;
import com.v7878.dex.immutable.bytecode.iface.OneRegisterInstruction;
import com.v7878.dex.immutable.bytecode.iface.SingleReferenceInstruction;
import com.v7878.dex.util.Formatter;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public final class InstructionN1c extends Instruction
        implements OneRegisterInstruction, SingleReferenceInstruction {
    private final int register1;
    private final Object reference1;

    private InstructionN1c(Opcode opcode, int register1, Object reference1) {
        super(Preconditions.checkFormat(opcode, "N1c",
                Format21c, Format31c, Format41c));
        this.register1 = switch (opcode.format()) {
            case Format21c, Format31c -> Preconditions.checkByteRegister(register1);
            case Format41c -> Preconditions.checkShortRegister(register1);
            default -> throw shouldNotReachHere();
        };
        this.reference1 = ReferenceType.validate(getReferenceType1(), reference1);
    }

    public static InstructionN1c of(Opcode opcode, int register1, Object reference1) {
        return new InstructionN1c(opcode, register1, reference1);
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
        return obj instanceof InstructionN1c other
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
