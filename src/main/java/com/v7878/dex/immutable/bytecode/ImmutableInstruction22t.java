package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Format.Format22t;

import com.v7878.dex.Opcode;
import com.v7878.dex.base.bytecode.BaseInstruction22t;
import com.v7878.dex.iface.bytecode.formats.Instruction22t;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public class ImmutableInstruction22t extends BaseInstruction22t implements ImmutableInstruction {
    private final int register1;
    private final int register2;
    private final int branch_offset;

    protected ImmutableInstruction22t(
            Opcode opcode, int register1, int register2, int branch_offset) {
        super(Preconditions.checkFormat(opcode, Format22t));
        this.register1 = Preconditions.checkNibbleRegister(register1);
        this.register2 = Preconditions.checkNibbleRegister(register2);
        this.branch_offset = Preconditions.checkShortCodeOffset(branch_offset);
    }

    public static ImmutableInstruction22t of(
            Opcode opcode, int register1, int register2, int branch_offset) {
        return new ImmutableInstruction22t(opcode, register1, register2, branch_offset);
    }

    public static ImmutableInstruction22t of(Instruction22t other) {
        if (other instanceof ImmutableInstruction22t immutable) return immutable;
        return new ImmutableInstruction22t(other.getOpcode(),
                other.getRegister1(), other.getRegister2(), other.getBranchOffset());
    }

    @Override
    public int getRegister1() {
        return register1;
    }

    @Override
    public int getRegister2() {
        return register2;
    }

    @Override
    public int getBranchOffset() {
        return branch_offset;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOpcode(), getRegister1(), getRegister2(), getBranchOffset());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Instruction22t other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getRegister1() == other.getRegister1()
                && getRegister2() == other.getRegister2()
                && getBranchOffset() == other.getBranchOffset();
    }
}
