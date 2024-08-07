package com.v7878.dex.immutable.bytecode;

import com.v7878.dex.Opcode;
import com.v7878.dex.base.bytecode.BaseInstruction31t;
import com.v7878.dex.iface.bytecode.formats.Instruction31t;
import com.v7878.dex.util.Preconditions;

public class ImmutableInstruction31t extends BaseInstruction31t implements ImmutableInstruction {
    private final int register1;
    private final int branch_offset;

    protected ImmutableInstruction31t(Opcode opcode, int register1, int branch_offset) {
        super(opcode);
        this.register1 = Preconditions.checkByteRegister(register1);
        this.branch_offset = branch_offset;
    }

    public static ImmutableInstruction31t of(Opcode opcode, int register1, int branch_offset) {
        return new ImmutableInstruction31t(opcode, register1, branch_offset);
    }

    public static ImmutableInstruction31t of(Instruction31t other) {
        if (other instanceof ImmutableInstruction31t immutable) return immutable;
        return new ImmutableInstruction31t(other.getOpcode(),
                other.getRegister1(), other.getBranchOffset());
    }

    @Override
    public int getRegister1() {
        return register1;
    }

    @Override
    public int getBranchOffset() {
        return branch_offset;
    }
}
