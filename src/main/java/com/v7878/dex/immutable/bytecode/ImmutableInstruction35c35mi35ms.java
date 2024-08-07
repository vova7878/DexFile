package com.v7878.dex.immutable.bytecode;

import com.v7878.dex.Opcode;
import com.v7878.dex.base.bytecode.BaseInstruction35c35mi35ms;
import com.v7878.dex.iface.bytecode.formats.Instruction35c35mi35ms;
import com.v7878.dex.immutable.ImmutableReferenceFactory;
import com.v7878.dex.util.Preconditions;

public class ImmutableInstruction35c35mi35ms extends BaseInstruction35c35mi35ms implements ImmutableInstruction {
    private final int register_count;
    private final int register1;
    private final int register2;
    private final int register3;
    private final int register4;
    private final int register5;
    private final Object reference1;

    protected ImmutableInstruction35c35mi35ms(Opcode opcode, int register_count, int register1,
                                              int register2, int register3, int register4,
                                              int register5, Object reference1) {
        super(opcode);
        this.register_count = Preconditions.check35cAnd45ccRegisterCount(register_count);
        this.register1 = (register_count > 0) ? Preconditions.checkNibbleRegister(register1) : 0;
        this.register2 = (register_count > 1) ? Preconditions.checkNibbleRegister(register2) : 0;
        this.register3 = (register_count > 2) ? Preconditions.checkNibbleRegister(register3) : 0;
        this.register4 = (register_count > 3) ? Preconditions.checkNibbleRegister(register4) : 0;
        this.register5 = (register_count > 4) ? Preconditions.checkNibbleRegister(register5) : 0;
        this.reference1 = ImmutableReferenceFactory.of(opcode.getReferenceType1(), reference1);
    }

    public static ImmutableInstruction35c35mi35ms of(Opcode opcode, int register_count, int register1,
                                                     int register2, int register3, int register4,
                                                     int register5, Object reference1) {
        return new ImmutableInstruction35c35mi35ms(opcode, register_count,
                register1, register2, register3, register4, register5, reference1);
    }

    public static ImmutableInstruction35c35mi35ms of(Instruction35c35mi35ms other) {
        if (other instanceof ImmutableInstruction35c35mi35ms immutable) return immutable;
        return new ImmutableInstruction35c35mi35ms(other.getOpcode(), other.getRegisterCount(),
                other.getRegister1(), other.getRegister2(), other.getRegister3(),
                other.getRegister4(), other.getRegister5(), other.getReference1());
    }

    @Override
    public int getRegisterCount() {
        return register_count;
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
    public int getRegister3() {
        return register3;
    }

    @Override
    public int getRegister4() {
        return register4;
    }

    @Override
    public int getRegister5() {
        return register5;
    }

    @Override
    public Object getReference1() {
        return reference1;
    }
}
