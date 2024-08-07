package com.v7878.dex.immutable.bytecode;

import com.v7878.dex.Opcode;
import com.v7878.dex.base.bytecode.BaseInstruction21lh;
import com.v7878.dex.iface.bytecode.formats.Instruction21lh;
import com.v7878.dex.util.Preconditions;

public class ImmutableInstruction21lh extends BaseInstruction21lh implements ImmutableInstruction {
    private final int register1;
    private final long literal;

    protected ImmutableInstruction21lh(Opcode opcode, int register1, long literal) {
        super(opcode);
        this.register1 = Preconditions.checkByteRegister(register1);
        this.literal = Preconditions.checkLongHatLiteral(literal);
    }

    public static ImmutableInstruction21lh of(Opcode opcode, int register1, long literal) {
        return new ImmutableInstruction21lh(opcode, register1, literal);
    }

    public static ImmutableInstruction21lh of(Instruction21lh other) {
        if (other instanceof ImmutableInstruction21lh immutable) return immutable;
        return new ImmutableInstruction21lh(other.getOpcode(),
                other.getRegister1(), other.getWideLiteral());
    }

    @Override
    public int getRegister1() {
        return register1;
    }

    @Override
    public long getWideLiteral() {
        return literal;
    }
}
