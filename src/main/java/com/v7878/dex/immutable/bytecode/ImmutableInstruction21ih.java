package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Format.Format21ih;

import com.v7878.dex.Opcode;
import com.v7878.dex.base.bytecode.BaseInstruction21ih;
import com.v7878.dex.iface.bytecode.formats.Instruction21ih;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public class ImmutableInstruction21ih extends BaseInstruction21ih implements ImmutableInstruction {
    private final int register1;
    private final int literal;

    protected ImmutableInstruction21ih(Opcode opcode, int register1, int literal) {
        super(Preconditions.checkFormat(opcode, Format21ih));
        this.register1 = Preconditions.checkByteRegister(register1);
        this.literal = Preconditions.checkIntegerHatLiteral(literal);
    }

    public static ImmutableInstruction21ih of(Opcode opcode, int register1, int literal) {
        return new ImmutableInstruction21ih(opcode, register1, literal);
    }

    public static ImmutableInstruction21ih of(Instruction21ih other) {
        if (other instanceof ImmutableInstruction21ih immutable) return immutable;
        return new ImmutableInstruction21ih(other.getOpcode(),
                other.getRegister1(), other.getLiteral());
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
        return obj instanceof Instruction21ih other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getRegister1() == other.getRegister1()
                && getLiteral() == other.getLiteral();
    }
}
