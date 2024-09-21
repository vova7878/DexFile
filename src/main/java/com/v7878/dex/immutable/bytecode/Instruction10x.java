package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Format.Format10x;

import com.v7878.dex.Opcode;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public class Instruction10x extends Instruction {
    protected Instruction10x(Opcode opcode) {
        super(Preconditions.checkFormat(opcode, Format10x));
    }

    public static Instruction10x of(Opcode opcode) {
        return new Instruction10x(opcode);
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
