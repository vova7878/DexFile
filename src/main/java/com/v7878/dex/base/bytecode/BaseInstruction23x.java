package com.v7878.dex.base.bytecode;

import static com.v7878.dex.Format.Format23x;

import com.v7878.dex.Opcode;
import com.v7878.dex.iface.bytecode.formats.Instruction23x;

import java.util.Objects;

public abstract class BaseInstruction23x implements Instruction23x {
    private final Opcode opcode;

    public BaseInstruction23x(Opcode opcode) {
        assert opcode.format() == Format23x;
        this.opcode = opcode;
    }

    @Override
    public final Opcode getOpcode() {
        return opcode;
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
