package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Format.Format20t;

import com.v7878.dex.Opcode;
import com.v7878.dex.immutable.bytecode.iface.BranchOffsetInstruction;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public final class Instruction20t extends Instruction implements BranchOffsetInstruction {
    private final int branch_offset;

    private Instruction20t(Opcode opcode, int branch_offset) {
        super(Preconditions.checkFormat(opcode, Format20t));
        this.branch_offset = Preconditions.checkShortCodeOffset(branch_offset);
    }

    public static Instruction20t of(Opcode opcode, int branch_offset) {
        return new Instruction20t(opcode, branch_offset);
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
        return obj instanceof Instruction20t other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getBranchOffset() == other.getBranchOffset();
    }
}
