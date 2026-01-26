package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Format.Format22t;

import com.v7878.dex.Opcode;
import com.v7878.dex.immutable.bytecode.iface.BranchOffsetInstruction;
import com.v7878.dex.immutable.bytecode.iface.TwoRegisterInstruction;
import com.v7878.dex.util.Formatter;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public final class Instruction22t extends Instruction
        implements TwoRegisterInstruction, BranchOffsetInstruction {
    private final int register1;
    private final int register2;
    private final int branch_offset;

    private Instruction22t(
            Opcode opcode, int register1, int register2, int branch_offset) {
        super(Preconditions.checkFormat(opcode, Format22t));
        this.register1 = Preconditions.checkNibbleRegister(register1);
        this.register2 = Preconditions.checkNibbleRegister(register2);
        this.branch_offset = Preconditions.checkShortCodeOffset(branch_offset);
    }

    public static Instruction22t of(
            Opcode opcode, int register1, int register2, int branch_offset) {
        return new Instruction22t(opcode, register1, register2, branch_offset);
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

    @Override
    public String toString() {
        return getName() + " " + Formatter.register(register1)
                + ", " + Formatter.register(register2)
                + ", " + Formatter.signedHex(branch_offset);
    }
}
