package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Opcode.RAW_REF;
import static com.v7878.dex.Opcode.RAW_REF_JUMBO;

import com.v7878.dex.Opcode;
import com.v7878.dex.ReferenceType;
import com.v7878.dex.immutable.bytecode.iface.SingleReferenceInstruction;

import java.util.Objects;

public final class InstructionRawRef extends Instruction implements SingleReferenceInstruction {
    private final ReferenceType type;
    private final Object reference;

    private InstructionRawRef(Opcode opcode, ReferenceType type, Object reference) {
        super(opcode);
        this.type = Objects.requireNonNull(type);
        this.reference = ReferenceType.validate(type, reference);
    }

    public static InstructionRawRef of(ReferenceType type, Object reference, boolean jumbo) {
        return new InstructionRawRef(jumbo ? RAW_REF_JUMBO : RAW_REF, type, reference);
    }

    @Override
    public ReferenceType getReferenceType1() {
        return type;
    }

    @Override
    public Object getReference1() {
        return reference;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOpcode(), type, reference);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof InstructionRawRef other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getReferenceType1() == other.getReferenceType1()
                && Objects.equals(getReference1(), other.getReference1());
    }

    @Override
    public String toString() {
        return getName() + " " + ReferenceType.describe(getReferenceType1(), reference);
    }
}
