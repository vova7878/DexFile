package com.v7878.dex.base.bytecode;

import static com.v7878.dex.Format.Format11n;

import com.v7878.dex.Opcode;
import com.v7878.dex.iface.bytecode.formats.Instruction11n;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public abstract class BaseInstruction11n extends BaseInstruction implements Instruction11n {
    public BaseInstruction11n(Opcode opcode) {
        super(Preconditions.checkFormat(opcode, Format11n));
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
