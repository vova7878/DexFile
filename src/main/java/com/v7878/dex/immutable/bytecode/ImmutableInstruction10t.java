package com.v7878.dex.immutable.bytecode;

import com.v7878.dex.Opcode;
import com.v7878.dex.base.bytecode.BaseInstruction10t;
import com.v7878.dex.iface.bytecode.formats.Instruction10t;
import com.v7878.dex.util.Preconditions;

public class ImmutableInstruction10t extends BaseInstruction10t implements ImmutableInstruction {
    private final int branch_offset;

    protected ImmutableInstruction10t(Opcode opcode, int branch_offset) {
        super(opcode);
        this.branch_offset = Preconditions.checkByteCodeOffset(branch_offset);
    }

    public static ImmutableInstruction10t of(Opcode opcode, int branch_offset) {
        return new ImmutableInstruction10t(opcode, branch_offset);
    }

    public static ImmutableInstruction10t of(Instruction10t other) {
        if (other instanceof ImmutableInstruction10t immutable) return immutable;
        return new ImmutableInstruction10t(other.getOpcode(), other.getBranchOffset());
    }

    @Override
    public int getBranchOffset() {
        return branch_offset;
    }
}
