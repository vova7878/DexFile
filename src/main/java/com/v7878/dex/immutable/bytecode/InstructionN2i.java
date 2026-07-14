package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Format.Format22b;
import static com.v7878.dex.Format.Format22s;
import static com.v7878.dex.util.Checks.shouldNotReachHere;

import com.v7878.dex.Opcode;
import com.v7878.dex.immutable.bytecode.iface.LiteralInstruction;
import com.v7878.dex.immutable.bytecode.iface.TwoRegisterInstruction;
import com.v7878.dex.util.Formatter;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public final class InstructionN2i extends Instruction
        implements TwoRegisterInstruction, LiteralInstruction {
    private final int register1;
    private final int register2;
    private final int literal;

    private InstructionN2i(Opcode opcode, int register1, int register2, int literal) {
        super(Preconditions.checkFormat(opcode, "N2i", Format22b, Format22s));
        this.register1 = switch (opcode.format()) {
            case Format22b -> Preconditions.checkByteRegister(register1);
            case Format22s -> Preconditions.checkNibbleRegister(register1);
            default -> throw shouldNotReachHere();
        };
        this.register2 = switch (opcode.format()) {
            case Format22b -> Preconditions.checkByteRegister(register2);
            case Format22s -> Preconditions.checkNibbleRegister(register2);
            default -> throw shouldNotReachHere();
        };
        this.literal = switch (opcode.format()) {
            case Format22b -> Preconditions.checkByteLiteral(literal);
            case Format22s -> Preconditions.checkShortLiteral(literal);
            default -> throw shouldNotReachHere();
        };
    }

    public static InstructionN2i of(
            Opcode opcode, int register1, int register2, int literal) {
        return new InstructionN2i(opcode, register1, register2, literal);
    }

    @Override
    public int getRegister1() {
        return register1;
    }

    @Override
    public int getRegister2() {
        return register2;
    }

    @Override
    public int getLiteral() {
        return literal;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOpcode(), getRegister1(), getRegister2(), getLiteral());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof InstructionN2i other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getRegister1() == other.getRegister1()
                && getRegister2() == other.getRegister2()
                && getLiteral() == other.getLiteral();
    }

    @Override
    public String toString() {
        return getName() + " " + Formatter.register(register1)
                + ", " + Formatter.register(register2)
                + ", " + Formatter.unsignedHex(literal);
    }
}
