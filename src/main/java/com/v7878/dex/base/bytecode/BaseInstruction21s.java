package com.v7878.dex.base.bytecode;

import static com.v7878.dex.Format.Format21s;

import com.v7878.dex.Opcode;
import com.v7878.dex.iface.bytecode.formats.Instruction21s;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public abstract class BaseInstruction21s extends BaseInstruction implements Instruction21s {
    public BaseInstruction21s(Opcode opcode) {
        super(Preconditions.checkFormat(opcode, Format21s));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOpcode(), getRegister1(), getLiteral());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Instruction21s other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getRegister1() == other.getRegister1()
                && getLiteral() == other.getLiteral();
    }
}
