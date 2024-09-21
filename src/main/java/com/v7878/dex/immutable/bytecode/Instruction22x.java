package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Format.Format22x;

import com.v7878.dex.Opcode;
import com.v7878.dex.immutable.bytecode.iface.TwoRegisterInstruction;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public final class Instruction22x extends Instruction implements TwoRegisterInstruction {
    private final int register1;
    private final int register2;

    private Instruction22x(Opcode opcode, int register1, int register2) {
        super(Preconditions.checkFormat(opcode, Format22x));
        this.register1 = Preconditions.checkByteRegister(register1);
        this.register2 = Preconditions.checkShortRegister(register2);
    }

    public static Instruction22x of(Opcode opcode, int register1, int register2) {
        return new Instruction22x(opcode, register1, register2);
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
        return obj instanceof Instruction22x other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getRegister1() == other.getRegister1()
                && getRegister2() == other.getRegister2();
    }
}
