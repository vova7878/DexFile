package com.v7878.dex.immutable.bytecode;

import com.v7878.dex.Opcode;
import com.v7878.dex.base.bytecode.BaseInstruction21ih;
import com.v7878.dex.iface.bytecode.formats.Instruction21ih;
import com.v7878.dex.util.Preconditions;

public class ImmutableInstruction21ih extends BaseInstruction21ih implements ImmutableInstruction {
    private final int register1;
    private final int literal;

    protected ImmutableInstruction21ih(Opcode opcode, int register1, int literal) {
        super(opcode);
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
}