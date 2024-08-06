package com.v7878.dex.base.bytecode;

import static com.v7878.dex.Format.Format31i;

import com.v7878.dex.Opcode;
import com.v7878.dex.iface.bytecode.formats.Instruction31i;

import java.util.Objects;

public abstract class BaseInstruction31i implements Instruction31i {
    private final Opcode opcode;

    public BaseInstruction31i(Opcode opcode) {
        assert opcode.format() == Format31i;
        this.opcode = opcode;
    }

    @Override
    public final Opcode getOpcode() {
        return opcode;
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
