package com.v7878.dex.base.bytecode;

import static com.v7878.dex.Format.Format11x;

import com.v7878.dex.Opcode;
import com.v7878.dex.iface.bytecode.formats.Instruction11x;

import java.util.Objects;

public abstract class BaseInstruction11x implements Instruction11x {
    private final Opcode opcode;

    public BaseInstruction11x(Opcode opcode) {
        assert opcode.format() == Format11x;
        this.opcode = opcode;
    }

    @Override
    public final Opcode getOpcode() {
        return opcode;
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
}
