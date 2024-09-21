package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Format.Format32x;

import com.v7878.dex.Opcode;
import com.v7878.dex.base.bytecode.BaseInstruction32x;
import com.v7878.dex.iface.bytecode.formats.Instruction32x;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public class ImmutableInstruction32x extends BaseInstruction32x implements ImmutableInstruction {
    private final int register1;
    private final int register2;

    protected ImmutableInstruction32x(Opcode opcode, int register1, int register2) {
        super(Preconditions.checkFormat(opcode, Format32x));
        this.register1 = Preconditions.checkShortRegister(register1);
        this.register2 = Preconditions.checkShortRegister(register2);
    }

    public static ImmutableInstruction32x of(Opcode opcode, int register1, int register2) {
        return new ImmutableInstruction32x(opcode, register1, register2);
    }

    public static ImmutableInstruction32x of(Instruction32x other) {
        if (other instanceof ImmutableInstruction32x immutable) return immutable;
        return new ImmutableInstruction32x(other.getOpcode(),
                other.getRegister1(), other.getRegister2());
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
        return obj instanceof Instruction32x other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getRegister1() == other.getRegister1()
                && getRegister2() == other.getRegister2();
    }
}
