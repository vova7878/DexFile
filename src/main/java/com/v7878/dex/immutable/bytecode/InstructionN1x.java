package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Format.Format11x;

import com.v7878.dex.Opcode;
import com.v7878.dex.immutable.bytecode.iface.OneRegisterInstruction;
import com.v7878.dex.util.Formatter;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public final class InstructionN1x extends Instruction implements OneRegisterInstruction {
    private final int register1;

    private InstructionN1x(Opcode opcode, int register1) {
        super(Preconditions.checkFormat(opcode, "N1x", Format11x));
        this.register1 = Preconditions.checkByteRegister(register1);
    }

    public static InstructionN1x of(Opcode opcode, int register1) {
        return new InstructionN1x(opcode, register1);
    }

    @Override
    public int getRegister1() {
        return register1;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOpcode(), getRegister1());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof InstructionN1x other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getRegister1() == other.getRegister1();
    }

    @Override
    public String toString() {
        return getName() + " " + Formatter.register(register1);
    }
}
