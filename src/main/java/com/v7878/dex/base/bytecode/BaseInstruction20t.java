package com.v7878.dex.base.bytecode;

import static com.v7878.dex.Format.Format20t;

import com.v7878.dex.Opcode;
import com.v7878.dex.iface.bytecode.formats.Instruction20t;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public abstract class BaseInstruction20t extends BaseInstruction implements Instruction20t {
    public BaseInstruction20t(Opcode opcode) {
        super(Preconditions.checkFormat(opcode, Format20t));
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
