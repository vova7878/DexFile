package com.v7878.dex.base.bytecode;

import static com.v7878.dex.Format.Format22t;

import com.v7878.dex.Opcode;
import com.v7878.dex.iface.bytecode.formats.Instruction22t;

import java.util.Objects;

public abstract class BaseInstruction22t implements Instruction22t {
    private final Opcode opcode;

    public BaseInstruction22t(Opcode opcode) {
        assert opcode.format() == Format22t;
        this.opcode = opcode;
    }

    @Override
    public final Opcode getOpcode() {
        return opcode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOpcode(), getRegister1(), getRegister2(), getBranchOffset());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Instruction22t other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getRegister1() == other.getRegister1()
                && getRegister2() == other.getRegister2()
                && getBranchOffset() == other.getBranchOffset();
    }
}
