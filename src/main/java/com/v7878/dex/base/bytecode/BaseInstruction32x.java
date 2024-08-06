package com.v7878.dex.base.bytecode;

import static com.v7878.dex.Format.Format32x;

import com.v7878.dex.Opcode;
import com.v7878.dex.iface.bytecode.formats.Instruction32x;

import java.util.Objects;

public abstract class BaseInstruction32x implements Instruction32x {
    private final Opcode opcode;

    public BaseInstruction32x(Opcode opcode) {
        assert opcode.format() == Format32x;
        this.opcode = opcode;
    }

    @Override
    public final Opcode getOpcode() {
        return opcode;
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
