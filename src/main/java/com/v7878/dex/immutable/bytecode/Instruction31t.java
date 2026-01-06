package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Format.Format31t;

import com.v7878.dex.Opcode;
import com.v7878.dex.immutable.bytecode.iface.BranchOffsetInstruction;
import com.v7878.dex.immutable.bytecode.iface.OneRegisterInstruction;
import com.v7878.dex.util.Formatter;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public final class Instruction31t extends Instruction
        implements OneRegisterInstruction, BranchOffsetInstruction {
    private final int register1;
    private final int branch_offset;

    private Instruction31t(Opcode opcode, int register1, int branch_offset) {
        super(Preconditions.checkFormat(opcode, Format31t));
        this.register1 = Preconditions.checkByteRegister(register1);
        this.branch_offset = branch_offset;
    }

    public static Instruction31t of(Opcode opcode, int register1, int branch_offset) {
        return new Instruction31t(opcode, register1, branch_offset);
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
        return obj instanceof Instruction31t other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getRegister1() == other.getRegister1()
                && getBranchOffset() == other.getBranchOffset();
    }

    @Override
    public String toString() {
        return getName() + " " + Formatter.register(register1)
                + ", " + Formatter.signedHex(branch_offset);
    }
}
