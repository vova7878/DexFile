package com.v7878.dex.base.bytecode;

import static com.v7878.dex.Format.Format20t;

import com.v7878.dex.Opcode;
import com.v7878.dex.iface.bytecode.formats.Instruction20t;

import java.util.Objects;

public abstract class BaseInstruction20t implements Instruction20t {
    private final Opcode opcode;

    public BaseInstruction20t(Opcode opcode) {
        assert opcode.format() == Format20t;
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
        return obj instanceof Instruction20t other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getBranchOffset() == other.getBranchOffset();
    }
}
