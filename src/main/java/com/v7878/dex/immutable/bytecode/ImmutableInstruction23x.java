package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Format.Format23x;

import com.v7878.dex.Opcode;
import com.v7878.dex.base.bytecode.BaseInstruction23x;
import com.v7878.dex.iface.bytecode.formats.Instruction23x;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public class ImmutableInstruction23x extends BaseInstruction23x implements ImmutableInstruction {
    private final int register1;
    private final int register2;
    private final int register3;

    protected ImmutableInstruction23x(
            Opcode opcode, int register1, int register2, int register3) {
        super(Preconditions.checkFormat(opcode, Format23x));
        this.register1 = Preconditions.checkByteRegister(register1);
        this.register2 = Preconditions.checkByteRegister(register2);
        this.register3 = Preconditions.checkByteRegister(register3);
    }

    public static ImmutableInstruction23x of(
            Opcode opcode, int register1, int register2, int register3) {
        return new ImmutableInstruction23x(opcode, register1, register2, register3);
    }

    public static ImmutableInstruction23x of(Instruction23x other) {
        if (other instanceof ImmutableInstruction23x immutable) return immutable;
        return new ImmutableInstruction23x(other.getOpcode(),
                other.getRegister1(), other.getRegister2(), other.getRegister3());
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
}
