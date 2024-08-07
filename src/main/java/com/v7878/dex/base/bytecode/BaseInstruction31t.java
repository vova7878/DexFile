package com.v7878.dex.base.bytecode;

import static com.v7878.dex.Format.Format31t;

import com.v7878.dex.Opcode;
import com.v7878.dex.iface.bytecode.formats.Instruction31t;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public abstract class BaseInstruction31t extends BaseInstruction implements Instruction31t {
    public BaseInstruction31t(Opcode opcode) {
        super(Preconditions.checkFormat(opcode, Format31t));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOpcode(), getRegister1(), getBranchOffset());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Instruction31t other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getRegister1() == other.getRegister1()
                && getBranchOffset() == other.getBranchOffset();
    }
}
