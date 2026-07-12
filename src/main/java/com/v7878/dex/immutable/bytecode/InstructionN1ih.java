package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Format.Format21ih;

import com.v7878.dex.Opcode;
import com.v7878.dex.immutable.bytecode.iface.LiteralInstruction;
import com.v7878.dex.immutable.bytecode.iface.OneRegisterInstruction;
import com.v7878.dex.util.Formatter;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public final class InstructionN1ih extends Instruction
        implements OneRegisterInstruction, LiteralInstruction {
    private final int register1;
    private final int literal;

    private InstructionN1ih(Opcode opcode, int register1, int literal) {
        super(Preconditions.checkFormat(opcode, "N1ih", Format21ih));
        this.register1 = Preconditions.checkByteRegister(register1);
        this.literal = Preconditions.checkIntegerHatLiteral(literal);
    }

    public static InstructionN1ih of(Opcode opcode, int register1, int literal) {
        return new InstructionN1ih(opcode, register1, literal);
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
        return obj instanceof InstructionN1ih other
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
