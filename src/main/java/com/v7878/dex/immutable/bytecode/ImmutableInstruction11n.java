package com.v7878.dex.immutable.bytecode;

import com.v7878.dex.Opcode;
import com.v7878.dex.base.bytecode.BaseInstruction11n;
import com.v7878.dex.iface.bytecode.formats.Instruction11n;
import com.v7878.dex.util.Preconditions;

public class ImmutableInstruction11n extends BaseInstruction11n implements ImmutableInstruction {
    private final int register1;
    private final int literal;

    protected ImmutableInstruction11n(Opcode opcode, int register1, int literal) {
        super(opcode);
        this.register1 = Preconditions.checkNibbleRegister(register1);
        this.literal = Preconditions.checkNibbleLiteral(literal);
    }

    public static ImmutableInstruction11n of(Opcode opcode, int register1, int literal) {
        return new ImmutableInstruction11n(opcode, register1, literal);
    }

    public static ImmutableInstruction11n of(Instruction11n other) {
        if (other instanceof ImmutableInstruction11n immutable) return immutable;
        return new ImmutableInstruction11n(other.getOpcode(),
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