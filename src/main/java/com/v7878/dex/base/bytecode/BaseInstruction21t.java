package com.v7878.dex.base.bytecode;

import static com.v7878.dex.Format.Format21t;

import com.v7878.dex.Opcode;
import com.v7878.dex.iface.bytecode.formats.Instruction21t;

import java.util.Objects;

public abstract class BaseInstruction21t implements Instruction21t {
    private final Opcode opcode;

    public BaseInstruction21t(Opcode opcode) {
        assert opcode.format() == Format21t;
        this.opcode = opcode;
    }

    @Override
    public final Opcode getOpcode() {
        return opcode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOpcode(), getRegister1(), getBranchOffset());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Instruction21t other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getRegister1() == other.getRegister1()
                && getBranchOffset() == other.getBranchOffset();
    }
}
