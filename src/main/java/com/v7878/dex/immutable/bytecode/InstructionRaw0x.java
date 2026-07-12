package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Opcode.RAW;
import static com.v7878.dex.Opcode.RAW_ALIGNED;

import com.v7878.dex.util.Formatter;

import java.util.Objects;

public final class InstructionRaw0x extends Instruction {
    private final short value;

    private InstructionRaw0x(short value, boolean aligned) {
        super(aligned ? RAW_ALIGNED : RAW);
        this.value = value;
    }

    public static InstructionRaw0x of(short value, boolean aligned) {
        return new InstructionRaw0x(value, aligned);
    }

    public static InstructionRaw0x of(short value) {
        return new InstructionRaw0x(value, false);
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
        return obj instanceof InstructionRaw0x other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getValue() == other.getValue();
    }

    @Override
    public String toString() {
        return getName() + " " + Formatter.unsignedHex(value & 0xffff);
    }
}
