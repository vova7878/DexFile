package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Format.Format10t;

import com.v7878.dex.Opcode;
import com.v7878.dex.base.bytecode.BaseInstruction10t;
import com.v7878.dex.iface.bytecode.formats.Instruction10t;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public class ImmutableInstruction10t extends BaseInstruction10t implements ImmutableInstruction {
    private final int branch_offset;

    protected ImmutableInstruction10t(Opcode opcode, int branch_offset) {
        super(Preconditions.checkFormat(opcode, Format10t));
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

    @Override
    public int hashCode() {
        return Objects.hash(getOpcode(), getBranchOffset());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Instruction10t other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getBranchOffset() == other.getBranchOffset();
    }
}
