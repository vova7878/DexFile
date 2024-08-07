package com.v7878.dex.base.bytecode;

import static com.v7878.dex.Format.Format30t;

import com.v7878.dex.Opcode;
import com.v7878.dex.iface.bytecode.formats.Instruction30t;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public abstract class BaseInstruction30t extends BaseInstruction implements Instruction30t {
    public BaseInstruction30t(Opcode opcode) {
        super(Preconditions.checkFormat(opcode, Format30t));
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
