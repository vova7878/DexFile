package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Format.Format20t_24;

import com.v7878.dex.Opcode;
import com.v7878.dex.immutable.bytecode.iface.BranchOffsetInstruction;
import com.v7878.dex.util.Formatter;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public final class Instruction20t_24 extends Instruction implements BranchOffsetInstruction {
    private final int branch_offset;

    private Instruction20t_24(Opcode opcode, int branch_offset) {
        super(Preconditions.checkFormat(opcode, Format20t_24));
        this.branch_offset = Preconditions.checkCodeOffset24(branch_offset);
    }

    public static Instruction20t_24 of(Opcode opcode, int branch_offset) {
        return new Instruction20t_24(opcode, branch_offset);
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
        return obj instanceof Instruction20t_24 other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getBranchOffset() == other.getBranchOffset();
    }

    @Override
    public String toString() {
        return getName() + " " + Formatter.signedHex(branch_offset);
    }
}
