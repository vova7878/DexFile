package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Format.Format10t;
import static com.v7878.dex.Format.Format20t;
import static com.v7878.dex.Format.Format20t_24;
import static com.v7878.dex.Format.Format30t;
import static com.v7878.dex.util.Checks.shouldNotReachHere;

import com.v7878.dex.Opcode;
import com.v7878.dex.immutable.bytecode.iface.BranchOffsetInstruction;
import com.v7878.dex.util.Formatter;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public final class InstructionN0t extends Instruction implements BranchOffsetInstruction {
    private final int branch_offset;

    private InstructionN0t(Opcode opcode, int branch_offset) {
        super(Preconditions.checkFormat(opcode, "N0t",
                Format10t, Format20t, Format20t_24, Format30t));
        this.branch_offset = switch (opcode.format()) {
            case Format10t -> Preconditions.checkByteCodeOffset(branch_offset);
            case Format20t -> Preconditions.checkShortCodeOffset(branch_offset);
            case Format20t_24 -> Preconditions.checkCodeOffset24(branch_offset);
            case Format30t -> branch_offset;
            default -> throw shouldNotReachHere();
        };
    }

    public static InstructionN0t of(Opcode opcode, int branch_offset) {
        return new InstructionN0t(opcode, branch_offset);
    }

    @Override
    public int getBranchOffset() {
        return branch_offset;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOpcode(), getBranchOffset());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof InstructionN0t other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getBranchOffset() == other.getBranchOffset();
    }

    @Override
    public String toString() {
        return getName() + " " + Formatter.signedHex(branch_offset);
    }
}
