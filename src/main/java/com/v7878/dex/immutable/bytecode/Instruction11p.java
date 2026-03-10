package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Format.Format11p;

import com.v7878.dex.Opcode;
import com.v7878.dex.immutable.bytecode.iface.IndexInstruction;
import com.v7878.dex.immutable.bytecode.iface.LiteralInstruction;
import com.v7878.dex.immutable.bytecode.iface.OneRegisterInstruction;
import com.v7878.dex.immutable.bytecode.iface.WideLiteralInstruction;
import com.v7878.dex.util.Formatter;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public final class Instruction11p extends Instruction implements OneRegisterInstruction,
        IndexInstruction, LiteralInstruction, WideLiteralInstruction {
    private static final int[] CONSTANTS_32 = {
            0xc0000000,
            0xbf800000,
            0xbf000000,
            0xbe800000,
            0xbdcccccd,
            0x3dcccccd,
            0x3e800000,
            0x3f000000,
            0x3f800000,
            0x40000000,
            0x40400000,
            0x40800000,
            0x40a00000,
            0x41200000,
            0x42c80000,
            0x447a0000
    };
    private static final long[] CONSTANTS_64 = {
            0xc000000000000000L,
            0xbff0000000000000L,
            0xbfe0000000000000L,
            0xbfd0000000000000L,
            0xbfb999999999999aL,
            0x3fb999999999999aL,
            0x3fd0000000000000L,
            0x3fe0000000000000L,
            0x3ff0000000000000L,
            0x4000000000000000L,
            0x4008000000000000L,
            0x4010000000000000L,
            0x4014000000000000L,
            0x4024000000000000L,
            0x4059000000000000L,
            0x408f400000000000L
    };

    private final int register1;
    private final int index;

    private Instruction11p(Opcode opcode, int register1, int index) {
        super(Preconditions.checkFormat(opcode, Format11p));
        this.register1 = Preconditions.checkNibbleRegister(register1);
        this.index = Preconditions.checkNibbleIndex(index);
    }

    public static Instruction11p of(Opcode opcode, int register1, int literal) {
        return new Instruction11p(opcode, register1, literal);
    }

    @Override
    public int getRegister1() {
        return register1;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public int getLiteral() {
        return CONSTANTS_32[index];
    }

    @Override
    public long getWideLiteral() {
        return CONSTANTS_64[index];
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOpcode(), getRegister1(), getLiteral());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Instruction11p other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getRegister1() == other.getRegister1()
                && getIndex() == other.getIndex();
    }

    @Override
    public String toString() {
        return getName() + " " + Formatter.register(register1)
                + ", " + Formatter.unsignedHex(index);
    }
}
