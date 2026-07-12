package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Format.Format11n;
import static com.v7878.dex.Format.Format21ih;
import static com.v7878.dex.Format.Format21s;
import static com.v7878.dex.Format.Format31i;
import static com.v7878.dex.util.Checks.shouldNotReachHere;

import com.v7878.dex.Opcode;
import com.v7878.dex.immutable.bytecode.iface.LiteralInstruction;
import com.v7878.dex.immutable.bytecode.iface.OneRegisterInstruction;
import com.v7878.dex.util.Formatter;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public final class InstructionN1i extends Instruction
        implements OneRegisterInstruction, LiteralInstruction {
    private final int register1;
    private final int literal;

    private InstructionN1i(Opcode opcode, int register1, int literal) {
        super(Preconditions.checkFormat(opcode, "N1i",
                Format11n, Format21ih, Format21s, Format31i));
        this.register1 = switch (opcode.format()) {
            case Format11n -> Preconditions.checkNibbleRegister(register1);
            case Format21ih, Format21s, Format31i -> Preconditions.checkByteRegister(register1);
            default -> throw shouldNotReachHere();
        };
        this.literal = switch (opcode.format()) {
            case Format11n -> Preconditions.checkNibbleLiteral(literal);
            case Format21ih -> Preconditions.checkIntegerHatLiteral(literal);
            case Format21s -> Preconditions.checkShortLiteral(literal);
            case Format31i -> literal;
            default -> throw shouldNotReachHere();
        };
    }

    public static InstructionN1i of(Opcode opcode, int register1, int literal) {
        return new InstructionN1i(opcode, register1, literal);
    }

    @Override
    public int getRegister1() {
        return register1;
    }

    @Override
    public int getLiteral() {
        return literal;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOpcode(), getRegister1(), getLiteral());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof InstructionN1i other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getRegister1() == other.getRegister1()
                && getLiteral() == other.getLiteral();
    }

    @Override
    public String toString() {
        return getName() + " " + Formatter.register(register1)
                + ", " + Formatter.unsignedHex(literal);
    }
}
