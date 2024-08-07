package com.v7878.dex.base.bytecode;

import static com.v7878.dex.Format.Format21c;

import com.v7878.dex.Opcode;
import com.v7878.dex.iface.bytecode.formats.Instruction21c;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public abstract class BaseInstruction21c extends BaseInstruction implements Instruction21c {
    public BaseInstruction21c(Opcode opcode) {
        super(Preconditions.checkFormat(opcode, Format21c));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOpcode(), getRegister1(), getReference1());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Instruction21c other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getRegister1() == other.getRegister1()
                && Objects.equals(getReference1(), other.getReference1());
    }
}
