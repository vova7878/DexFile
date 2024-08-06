package com.v7878.dex.base.bytecode;

import static com.v7878.dex.Format.Format22c22cs;

import com.v7878.dex.Opcode;
import com.v7878.dex.iface.bytecode.formats.Instruction22c22cs;

import java.util.Objects;

public abstract class BaseInstruction22c22cs implements Instruction22c22cs {
    private final Opcode opcode;

    public BaseInstruction22c22cs(Opcode opcode) {
        assert opcode.format() == Format22c22cs;
        this.opcode = opcode;
    }

    @Override
    public final Opcode getOpcode() {
        return opcode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOpcode(), getRegister1(), getRegister2(), getReference1());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Instruction22c22cs other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getRegister1() == other.getRegister1()
                && getRegister2() == other.getRegister2()
                && Objects.equals(getReference1(), other.getReference1());
    }
}
