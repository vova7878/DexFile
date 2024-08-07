package com.v7878.dex.base.bytecode;

import static com.v7878.dex.Format.Format31i;

import com.v7878.dex.Opcode;
import com.v7878.dex.iface.bytecode.formats.Instruction31i;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public abstract class BaseInstruction31i extends BaseInstruction implements Instruction31i {
    public BaseInstruction31i(Opcode opcode) {
        super(Preconditions.checkFormat(opcode, Format31i));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOpcode(), getRegister1(), getLiteral());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Instruction31i other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getRegister1() == other.getRegister1()
                && getLiteral() == other.getLiteral();
    }
}
