package com.v7878.dex.immutable.bytecode;

import com.v7878.dex.Opcode;
import com.v7878.dex.base.bytecode.BaseInstruction30t;
import com.v7878.dex.iface.bytecode.formats.Instruction30t;

public class ImmutableInstruction30t extends BaseInstruction30t implements ImmutableInstruction {
    private final int branch_offset;

    protected ImmutableInstruction30t(Opcode opcode, int branch_offset) {
        super(opcode);
        this.branch_offset = branch_offset;
    }

    public static ImmutableInstruction30t of(Opcode opcode, int branch_offset) {
        return new ImmutableInstruction30t(opcode, branch_offset);
    }

    public static ImmutableInstruction30t of(Instruction30t other) {
        if (other instanceof ImmutableInstruction30t immutable) return immutable;
        return new ImmutableInstruction30t(other.getOpcode(), other.getBranchOffset());
    }

    @Override
    public int getBranchOffset() {
        return branch_offset;
    }
}