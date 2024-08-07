package com.v7878.dex.base.bytecode;

import static com.v7878.dex.Format.Format51l;

import com.v7878.dex.Opcode;
import com.v7878.dex.iface.bytecode.formats.Instruction51l;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public abstract class BaseInstruction51l extends BaseInstruction implements Instruction51l {
    public BaseInstruction51l(Opcode opcode) {
        super(Preconditions.checkFormat(opcode, Format51l));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOpcode(), getRegister1(), getWideLiteral());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Instruction51l other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getRegister1() == other.getRegister1()
                && getWideLiteral() == other.getWideLiteral();
    }
}
