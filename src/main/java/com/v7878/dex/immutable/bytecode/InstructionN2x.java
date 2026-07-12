package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Format.Format12x;
import static com.v7878.dex.Format.Format22x;
import static com.v7878.dex.Format.Format32x;
import static com.v7878.dex.util.Checks.shouldNotReachHere;

import com.v7878.dex.Opcode;
import com.v7878.dex.immutable.bytecode.iface.TwoRegisterInstruction;
import com.v7878.dex.util.Formatter;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public final class InstructionN2x extends Instruction implements TwoRegisterInstruction {
    private final int register1;
    private final int register2;

    private InstructionN2x(Opcode opcode, int register1, int register2) {
        super(Preconditions.checkFormat(opcode, "N2x", Format12x, Format22x, Format32x));
        this.register1 = switch (opcode.format()) {
            case Format12x -> Preconditions.checkNibbleRegister(register1);
            case Format22x -> Preconditions.checkByteRegister(register1);
            case Format32x -> Preconditions.checkShortRegister(register1);
            default -> throw shouldNotReachHere();
        };
        this.register2 = switch (opcode.format()) {
            case Format12x -> Preconditions.checkNibbleRegister(register2);
            case Format22x, Format32x -> Preconditions.checkShortRegister(register2);
            default -> throw shouldNotReachHere();
        };
    }

    public static InstructionN2x of(Opcode opcode, int register1, int register2) {
        return new InstructionN2x(opcode, register1, register2);
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
    public int hashCode() {
        return Objects.hash(getOpcode(), getRegister1(), getRegister2());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof InstructionN2x other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getRegister1() == other.getRegister1()
                && getRegister2() == other.getRegister2();
    }

    @Override
    public String toString() {
        return getName() + " " + Formatter.register(register1)
                + ", " + Formatter.register(register2);
    }
}
