package com.v7878.dex.immutable.bytecode;

import com.v7878.dex.Opcode;
import com.v7878.dex.base.bytecode.BaseInstruction22x;
import com.v7878.dex.iface.bytecode.formats.Instruction22x;
import com.v7878.dex.util.Preconditions;

public class ImmutableInstruction22x extends BaseInstruction22x implements ImmutableInstruction {
    private final int register1;
    private final int register2;

    protected ImmutableInstruction22x(Opcode opcode, int register1, int register2) {
        super(opcode);
        this.register1 = Preconditions.checkByteRegister(register1);
        this.register2 = Preconditions.checkShortRegister(register2);
    }

    public static ImmutableInstruction22x of(Opcode opcode, int register1, int register2) {
        return new ImmutableInstruction22x(opcode, register1, register2);
    }

    public static ImmutableInstruction22x of(Instruction22x other) {
        if (other instanceof ImmutableInstruction22x immutable) return immutable;
        return new ImmutableInstruction22x(other.getOpcode(),
                other.getRegister1(), other.getRegister2());
    }

    @Override
    public int getRegister1() {
        return register1;
    }

    @Override
    public int getRegister2() {
        return register2;
    }
}
