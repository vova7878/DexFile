package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Format.FormatRaw10c;
import static com.v7878.dex.Format.FormatRaw20c;
import static com.v7878.dex.Format.FormatWrapper40ci;
import static com.v7878.dex.Opcode.choose_raw_opcode;

import com.v7878.dex.Opcode;
import com.v7878.dex.ReferenceType;
import com.v7878.dex.immutable.bytecode.iface.RawInstruction;
import com.v7878.dex.immutable.bytecode.iface.SingleReferenceInstruction;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public final class InstructionRaw0c extends Instruction
        implements SingleReferenceInstruction, RawInstruction<InstructionRaw0c> {
    private final ReferenceType type;
    private final Object reference;

    private InstructionRaw0c(Opcode opcode, ReferenceType type, Object reference) {
        super(Preconditions.checkFormat(opcode, "raw0c",
                FormatRaw10c, FormatRaw20c, FormatWrapper40ci));
        this.type = Objects.requireNonNull(type);
        this.reference = ReferenceType.validate(type, reference);
    }

    public static InstructionRaw0c of(Opcode opcode, ReferenceType type, Object reference) {
        return new InstructionRaw0c(opcode, type, reference);
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
        return obj instanceof InstructionRaw0c other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getReferenceType1() == other.getReferenceType1()
                && Objects.equals(getReference1(), other.getReference1());
    }

    @Override
    public String toString() {
        return getName() + " " + ReferenceType.describe(getReferenceType1(), reference);
    }

    @Override
    public InstructionRaw0c wrapper() {
        return InstructionRaw0c.of(choose_raw_opcode(getOpcode(), true), type, reference);
    }

    @Override
    public InstructionRaw0c raw() {
        return InstructionRaw0c.of(choose_raw_opcode(getOpcode(), false), type, reference);
    }
}
