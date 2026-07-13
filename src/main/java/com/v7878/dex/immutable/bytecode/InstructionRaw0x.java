package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Format.FormatRaw10x;
import static com.v7878.dex.Format.FormatWrapper20x;
import static com.v7878.dex.Opcode.choose_raw_opcode;

import com.v7878.dex.Opcode;
import com.v7878.dex.immutable.bytecode.iface.RawInstruction;
import com.v7878.dex.util.Formatter;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public final class InstructionRaw0x extends Instruction
        implements RawInstruction<InstructionRaw0x> {
    private final short value;

    private InstructionRaw0x(Opcode opcode, short value) {
        super(Preconditions.checkFormat(opcode, "raw0x",
                FormatRaw10x, FormatWrapper20x));
        this.value = value;
    }

    public static InstructionRaw0x of(Opcode opcode, short value) {
        return new InstructionRaw0x(opcode, value);
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

    @Override
    public InstructionRaw0x wrapper() {
        return InstructionRaw0x.of(choose_raw_opcode(getOpcode(), true), value);
    }

    @Override
    public InstructionRaw0x raw() {
        return InstructionRaw0x.of(choose_raw_opcode(getOpcode(), false), value);
    }
}
