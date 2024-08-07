package com.v7878.dex.base.bytecode;

import static com.v7878.dex.Format.Format10t;

import com.v7878.dex.Opcode;
import com.v7878.dex.iface.bytecode.formats.Instruction10t;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public abstract class BaseInstruction10t extends BaseInstruction implements Instruction10t {
    public BaseInstruction10t(Opcode opcode) {
        super(Preconditions.checkFormat(opcode, Format10t));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOpcode(), getBranchOffset());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Instruction10t other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getBranchOffset() == other.getBranchOffset();
    }
}
