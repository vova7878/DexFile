package com.v7878.dex.base.bytecode;

import static com.v7878.dex.Format.Format30t;

import com.v7878.dex.Opcode;
import com.v7878.dex.iface.bytecode.formats.Instruction30t;

import java.util.Objects;

public abstract class BaseInstruction30t implements Instruction30t {
    private final Opcode opcode;

    public BaseInstruction30t(Opcode opcode) {
        assert opcode.format() == Format30t;
        this.opcode = opcode;
    }

    @Override
    public final Opcode getOpcode() {
        return opcode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOpcode(), getBranchOffset());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Instruction30t other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getBranchOffset() == other.getBranchOffset();
    }
}
