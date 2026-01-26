package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Format.Format30t;

import com.v7878.dex.Opcode;
import com.v7878.dex.immutable.bytecode.iface.BranchOffsetInstruction;
import com.v7878.dex.util.Formatter;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public final class Instruction30t extends Instruction implements BranchOffsetInstruction {
    private final int branch_offset;

    private Instruction30t(Opcode opcode, int branch_offset) {
        super(Preconditions.checkFormat(opcode, Format30t));
        this.branch_offset = branch_offset;
    }

    public static Instruction30t of(Opcode opcode, int branch_offset) {
        return new Instruction30t(opcode, branch_offset);
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
        return obj instanceof Instruction30t other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getBranchOffset() == other.getBranchOffset();
    }

    @Override
    public String toString() {
        return getName() + " " + Formatter.signedHex(branch_offset);
    }
}
