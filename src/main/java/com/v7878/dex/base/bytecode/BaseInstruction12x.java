package com.v7878.dex.base.bytecode;

import static com.v7878.dex.Format.Format12x;

import com.v7878.dex.Opcode;
import com.v7878.dex.iface.bytecode.formats.Instruction12x;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public abstract class BaseInstruction12x extends BaseInstruction implements Instruction12x {
    public BaseInstruction12x(Opcode opcode) {
        super(Preconditions.checkFormat(opcode, Format12x));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOpcode(), getRegister1(), getRegister2());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Instruction12x other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getRegister1() == other.getRegister1()
                && getRegister2() == other.getRegister2();
    }
}
