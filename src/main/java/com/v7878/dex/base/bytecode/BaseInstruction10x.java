package com.v7878.dex.base.bytecode;

import static com.v7878.dex.Format.Format10x;

import com.v7878.dex.Opcode;
import com.v7878.dex.iface.bytecode.formats.Instruction10x;

import java.util.Objects;

public abstract class BaseInstruction10x implements Instruction10x {
    private final Opcode opcode;

    public BaseInstruction10x(Opcode opcode) {
        assert opcode.format() == Format10x;
        this.opcode = opcode;
    }

    @Override
    public final Opcode getOpcode() {
        return opcode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOpcode());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Instruction10x other
                && Objects.equals(getOpcode(), other.getOpcode());
    }
}
