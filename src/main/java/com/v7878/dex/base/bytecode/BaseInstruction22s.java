package com.v7878.dex.base.bytecode;

import static com.v7878.dex.Format.Format22s;

import com.v7878.dex.Opcode;
import com.v7878.dex.iface.bytecode.formats.Instruction22s;

import java.util.Objects;

public abstract class BaseInstruction22s implements Instruction22s {
    private final Opcode opcode;

    public BaseInstruction22s(Opcode opcode) {
        assert opcode.format() == Format22s;
        this.opcode = opcode;
    }

    @Override
    public final Opcode getOpcode() {
        return opcode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOpcode(), getRegister1(), getRegister2(), getLiteral());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Instruction22s other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getRegister1() == other.getRegister1()
                && getRegister2() == other.getRegister2()
                && getLiteral() == other.getLiteral();
    }
}
