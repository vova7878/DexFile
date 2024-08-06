package com.v7878.dex.base.bytecode;

import static com.v7878.dex.Format.Format22x;

import com.v7878.dex.Opcode;
import com.v7878.dex.iface.bytecode.formats.Instruction22x;

import java.util.Objects;

public abstract class BaseInstruction22x implements Instruction22x {
    private final Opcode opcode;

    public BaseInstruction22x(Opcode opcode) {
        assert opcode.format() == Format22x;
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
        return obj instanceof Instruction22x other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getRegister1() == other.getRegister1()
                && getRegister2() == other.getRegister2();
    }
}
