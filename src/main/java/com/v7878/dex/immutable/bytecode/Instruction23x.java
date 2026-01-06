package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Format.Format23x;

import com.v7878.dex.Opcode;
import com.v7878.dex.immutable.bytecode.iface.ThreeRegisterInstruction;
import com.v7878.dex.util.Formatter;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public final class Instruction23x extends Instruction implements ThreeRegisterInstruction {
    private final int register1;
    private final int register2;
    private final int register3;

    private Instruction23x(
            Opcode opcode, int register1, int register2, int register3) {
        super(Preconditions.checkFormat(opcode, Format23x));
        this.register1 = Preconditions.checkByteRegister(register1);
        this.register2 = Preconditions.checkByteRegister(register2);
        this.register3 = Preconditions.checkByteRegister(register3);
    }

    public static Instruction23x of(
            Opcode opcode, int register1, int register2, int register3) {
        return new Instruction23x(opcode, register1, register2, register3);
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
    public int getRegister3() {
        return register3;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOpcode(), getRegister1(), getRegister2(), getRegister3());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Instruction23x other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getRegister1() == other.getRegister1()
                && getRegister2() == other.getRegister2()
                && getRegister3() == other.getRegister3();
    }

    @Override
    public String toString() {
        return getName() + " " + Formatter.register(register1)
                + ", " + Formatter.register(register2)
                + ", " + Formatter.register(register3);
    }
}
