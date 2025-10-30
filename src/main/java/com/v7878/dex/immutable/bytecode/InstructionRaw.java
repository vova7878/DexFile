package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Opcode.RAW;

import java.util.Objects;

public final class InstructionRaw extends Instruction {
    private final short value;

    private InstructionRaw(short value) {
        super(RAW);
        this.value = value;
    }

    public static InstructionRaw of(short value) {
        return new InstructionRaw(value);
    }

    public int getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOpcode(), value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof InstructionRaw other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getValue() == other.getValue();
    }
}
