package com.v7878.dex.base.bytecode;

import static com.v7878.dex.Format.Format22c22cs;

import com.v7878.dex.Opcode;
import com.v7878.dex.iface.bytecode.formats.Instruction22c22cs;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public abstract class BaseInstruction22c22cs extends BaseInstruction implements Instruction22c22cs {
    public BaseInstruction22c22cs(Opcode opcode) {
        super(Preconditions.checkFormat(opcode, Format22c22cs));
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
