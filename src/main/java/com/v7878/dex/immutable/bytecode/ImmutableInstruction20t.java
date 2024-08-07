package com.v7878.dex.immutable.bytecode;

import com.v7878.dex.Opcode;
import com.v7878.dex.base.bytecode.BaseInstruction20t;
import com.v7878.dex.iface.bytecode.formats.Instruction20t;
import com.v7878.dex.util.Preconditions;

public class ImmutableInstruction20t extends BaseInstruction20t implements ImmutableInstruction {
    private final int branch_offset;

    protected ImmutableInstruction20t(Opcode opcode, int branch_offset) {
        super(opcode);
        this.branch_offset = Preconditions.checkShortCodeOffset(branch_offset);
    }

    public static ImmutableInstruction20t of(Opcode opcode, int branch_offset) {
        return new ImmutableInstruction20t(opcode, branch_offset);
    }

    public static ImmutableInstruction20t of(Instruction20t other) {
        if (other instanceof ImmutableInstruction20t immutable) return immutable;
        return new ImmutableInstruction20t(other.getOpcode(), other.getBranchOffset());
    }

    @Override
    public int getBranchOffset() {
        return branch_offset;
    }
}
