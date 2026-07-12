package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Opcode.RAW;
import static com.v7878.dex.Opcode.RAW_ALIGNED;

import com.v7878.dex.util.Formatter;

import java.util.Objects;

public final class InstructionRaw extends Instruction {
    private final short value;

    private InstructionRaw(short value, boolean aligned) {
        super(aligned ? RAW_ALIGNED : RAW);
        this.value = value;
    }

    public static InstructionRaw of(short value, boolean aligned) {
        return new InstructionRaw(value, aligned);
    }

    public static InstructionRaw of(short value) {
        return new InstructionRaw(value, false);
    }

    public short getValue() {
        return value;
    }

    public boolean isAligned() {
        return getOpcode().isAligned();
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

    @Override
    public String toString() {
        return getName() + " " + Formatter.unsignedHex(value & 0xffff);
    }
}
