package com.v7878.dex.base.bytecode;

import static com.v7878.dex.Format.Format12x;

import com.v7878.dex.Opcode;
import com.v7878.dex.iface.bytecode.formats.Instruction12x;

import java.util.Objects;

public abstract class BaseInstruction12x implements Instruction12x {
    private final Opcode opcode;

    public BaseInstruction12x(Opcode opcode) {
        assert opcode.format() == Format12x;
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
        return obj instanceof Instruction12x other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getRegister1() == other.getRegister1()
                && getRegister2() == other.getRegister2();
    }
}
