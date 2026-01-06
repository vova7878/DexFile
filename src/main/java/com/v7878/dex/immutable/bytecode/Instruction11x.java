package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Format.Format11x;

import com.v7878.dex.Opcode;
import com.v7878.dex.immutable.bytecode.iface.OneRegisterInstruction;
import com.v7878.dex.util.Formatter;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public final class Instruction11x extends Instruction implements OneRegisterInstruction {
    private final int register1;

    private Instruction11x(Opcode opcode, int register1) {
        super(Preconditions.checkFormat(opcode, Format11x));
        this.register1 = Preconditions.checkByteRegister(register1);
    }

    public static Instruction11x of(Opcode opcode, int register1) {
        return new Instruction11x(opcode, register1);
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
        return obj instanceof Instruction11x other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getRegister1() == other.getRegister1();
    }

    @Override
    public String toString() {
        return getName() + " " + Formatter.register(register1);
    }
}
