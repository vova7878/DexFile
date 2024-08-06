package com.v7878.dex.base.bytecode;

import static com.v7878.dex.Format.Format11n;

import com.v7878.dex.Opcode;
import com.v7878.dex.iface.bytecode.formats.Instruction11n;

import java.util.Objects;

public abstract class BaseInstruction11n implements Instruction11n {
    private final Opcode opcode;

    public BaseInstruction11n(Opcode opcode) {
        assert opcode.format() == Format11n;
        this.opcode = opcode;
    }

    @Override
    public final Opcode getOpcode() {
        return opcode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOpcode(), getRegister1(), getLiteral());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Instruction11n other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getRegister1() == other.getRegister1()
                && getLiteral() == other.getLiteral();
    }
}
