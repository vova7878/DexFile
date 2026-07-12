package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Format.Format10x;

import com.v7878.dex.Opcode;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public final class InstructionN0x extends Instruction {
    private InstructionN0x(Opcode opcode) {
        super(Preconditions.checkFormat(opcode, "N0x", Format10x));
    }

    public static InstructionN0x of(Opcode opcode) {
        return new InstructionN0x(opcode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOpcode());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof InstructionN0x other
                && Objects.equals(getOpcode(), other.getOpcode());
    }

    @Override
    public String toString() {
        return getName();
    }
}
