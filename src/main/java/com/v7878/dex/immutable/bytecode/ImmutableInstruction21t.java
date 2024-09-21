package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Format.Format21t;

import com.v7878.dex.Opcode;
import com.v7878.dex.base.bytecode.BaseInstruction21t;
import com.v7878.dex.iface.bytecode.formats.Instruction21t;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public class ImmutableInstruction21t extends BaseInstruction21t implements ImmutableInstruction {
    private final int register1;
    private final int branch_offset;

    protected ImmutableInstruction21t(Opcode opcode, int register1, int branch_offset) {
        super(Preconditions.checkFormat(opcode, Format21t));
        this.register1 = Preconditions.checkByteRegister(register1);
        this.branch_offset = Preconditions.checkShortCodeOffset(branch_offset);
    }

    public static ImmutableInstruction21t of(Opcode opcode, int register1, int branch_offset) {
        return new ImmutableInstruction21t(opcode, register1, branch_offset);
    }

    public static ImmutableInstruction21t of(Instruction21t other) {
        if (other instanceof ImmutableInstruction21t immutable) return immutable;
        return new ImmutableInstruction21t(other.getOpcode(),
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

    @Override
    public int hashCode() {
        return Objects.hash(getOpcode(), getRegister1(), getBranchOffset());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Instruction21t other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getRegister1() == other.getRegister1()
                && getBranchOffset() == other.getBranchOffset();
    }
}
