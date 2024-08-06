package com.v7878.dex.base.bytecode;

import static com.v7878.dex.Format.Format31c;

import com.v7878.dex.Opcode;
import com.v7878.dex.iface.bytecode.formats.Instruction31c;

import java.util.Objects;

public abstract class BaseInstruction31c implements Instruction31c {
    private final Opcode opcode;

    public BaseInstruction31c(Opcode opcode) {
        assert opcode.format() == Format31c;
        this.opcode = opcode;
    }

    @Override
    public final Opcode getOpcode() {
        return opcode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOpcode(), getRegister1(), getReference1());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Instruction31c other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getRegister1() == other.getRegister1()
                && Objects.equals(getReference1(), other.getReference1());
    }
}
