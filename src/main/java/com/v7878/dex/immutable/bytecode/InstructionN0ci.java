package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Format.Format20bc;
import static com.v7878.dex.Format.Format40sc;

import com.v7878.dex.Opcode;
import com.v7878.dex.ReferenceType;
import com.v7878.dex.VerificationError;
import com.v7878.dex.immutable.bytecode.iface.SingleReferenceInstruction;
import com.v7878.dex.immutable.bytecode.iface.VerificationErrorInstruction;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public final class InstructionN0ci extends Instruction
        implements SingleReferenceInstruction, VerificationErrorInstruction {
    private final VerificationError error;
    private final ReferenceType type;
    private final Object reference;

    private InstructionN0ci(Opcode opcode, VerificationError error,
                            ReferenceType type, Object reference) {
        super(Preconditions.checkFormat(opcode, "N0ci", Format20bc, Format40sc));
        this.error = Objects.requireNonNull(error);
        this.type = checkReferenceType(type);
        this.reference = ReferenceType.validate(type, reference);
    }

    public static InstructionN0ci of(Opcode opcode, VerificationError error,
                                     ReferenceType type, Object reference) {
        return new InstructionN0ci(opcode, error, type, reference);
    }

    private static ReferenceType checkReferenceType(ReferenceType type) {
        Objects.requireNonNull(type);
        if (type != ReferenceType.TYPE && type != ReferenceType.FIELD
                && type != ReferenceType.METHOD) {
            throw new IllegalArgumentException(
                    "Illegal verification error reference type: " + type);
        }
        return type;
    }

    @Override
    public VerificationError getError() {
        return error;
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
        return Objects.hash(getOpcode(), error, type, reference);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof InstructionN0ci other
                && Objects.equals(getOpcode(), other.getOpcode())
                && error == other.error
                && type == other.type
                && Objects.equals(reference, other.reference);
    }

    @Override
    public String toString() {
        return getName() + " " + error + ", "
                + ReferenceType.describe(type, reference);
    }
}
