package com.v7878.dex.base.bytecode;

import static com.v7878.dex.Format.Format23x;

import com.v7878.dex.Opcode;
import com.v7878.dex.iface.bytecode.formats.Instruction23x;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public abstract class BaseInstruction23x extends BaseInstruction implements Instruction23x {
    public BaseInstruction23x(Opcode opcode) {
        super(Preconditions.checkFormat(opcode, Format23x));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOpcode(), getRegister1(), getRegister2(), getRegister3());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Instruction23x other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getRegister1() == other.getRegister1()
                && getRegister2() == other.getRegister2()
                && getRegister3() == other.getRegister3();
    }
}
