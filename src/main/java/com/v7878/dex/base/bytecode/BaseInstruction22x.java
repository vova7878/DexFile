package com.v7878.dex.base.bytecode;

import static com.v7878.dex.Format.Format22x;

import com.v7878.dex.Opcode;
import com.v7878.dex.iface.bytecode.formats.Instruction22x;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public abstract class BaseInstruction22x extends BaseInstruction implements Instruction22x {
    public BaseInstruction22x(Opcode opcode) {
        super(Preconditions.checkFormat(opcode, Format22x));
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
