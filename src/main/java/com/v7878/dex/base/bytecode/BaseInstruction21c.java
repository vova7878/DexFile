package com.v7878.dex.base.bytecode;

import static com.v7878.dex.Format.Format21c;

import com.v7878.dex.Opcode;
import com.v7878.dex.iface.bytecode.formats.Instruction21c;

import java.util.Objects;

public abstract class BaseInstruction21c implements Instruction21c {
    private final Opcode opcode;

    public BaseInstruction21c(Opcode opcode) {
        assert opcode.format() == Format21c;
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
        return obj instanceof Instruction21c other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getRegister1() == other.getRegister1()
                && Objects.equals(getReference1(), other.getReference1());
    }
}
