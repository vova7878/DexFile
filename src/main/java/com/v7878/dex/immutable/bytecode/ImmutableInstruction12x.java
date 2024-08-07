package com.v7878.dex.immutable.bytecode;

import com.v7878.dex.Opcode;
import com.v7878.dex.base.bytecode.BaseInstruction12x;
import com.v7878.dex.iface.bytecode.formats.Instruction12x;
import com.v7878.dex.util.Preconditions;

public class ImmutableInstruction12x extends BaseInstruction12x implements ImmutableInstruction {
    private final int register1;
    private final int register2;

    protected ImmutableInstruction12x(Opcode opcode, int register1, int register2) {
        super(opcode);
        this.register1 = Preconditions.checkNibbleRegister(register1);
        this.register2 = Preconditions.checkNibbleRegister(register2);
    }

    public static ImmutableInstruction12x of(Opcode opcode, int register1, int register2) {
        return new ImmutableInstruction12x(opcode, register1, register2);
    }

    public static ImmutableInstruction12x of(Instruction12x other) {
        if (other instanceof ImmutableInstruction12x immutable) return immutable;
        return new ImmutableInstruction12x(other.getOpcode(),
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
