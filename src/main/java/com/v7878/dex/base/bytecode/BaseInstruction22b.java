package com.v7878.dex.base.bytecode;

import static com.v7878.dex.Format.Format22b;

import com.v7878.dex.Opcode;
import com.v7878.dex.iface.bytecode.formats.Instruction22b;

import java.util.Objects;

public abstract class BaseInstruction22b implements Instruction22b {
    private final Opcode opcode;

    public BaseInstruction22b(Opcode opcode) {
        assert opcode.format() == Format22b;
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
        return obj instanceof Instruction22b other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getRegister1() == other.getRegister1()
                && getRegister2() == other.getRegister2()
                && getLiteral() == other.getLiteral();
    }
}
