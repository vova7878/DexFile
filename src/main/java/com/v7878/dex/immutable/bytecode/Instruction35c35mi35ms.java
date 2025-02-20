package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Format.Format35c35mi35ms;

import com.v7878.dex.Opcode;
import com.v7878.dex.ReferenceType;
import com.v7878.dex.immutable.bytecode.iface.SingleReferenceInstruction;
import com.v7878.dex.immutable.bytecode.iface.VariableFiveRegisterInstruction;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public final class Instruction35c35mi35ms extends Instruction
        implements VariableFiveRegisterInstruction, SingleReferenceInstruction {
    private final int register_count;
    private final int register1;
    private final int register2;
    private final int register3;
    private final int register4;
    private final int register5;
    private final Object reference1;

    private Instruction35c35mi35ms(Opcode opcode, int register_count, int register1,
                                   int register2, int register3, int register4,
                                   int register5, Object reference1) {
        super(Preconditions.checkFormat(opcode, Format35c35mi35ms));
        this.register_count = Preconditions.check35cAnd45ccRegisterCount(register_count);
        this.register1 = (register_count > 0) ? Preconditions.checkNibbleRegister(register1) : 0;
        this.register2 = (register_count > 1) ? Preconditions.checkNibbleRegister(register2) : 0;
        this.register3 = (register_count > 2) ? Preconditions.checkNibbleRegister(register3) : 0;
        this.register4 = (register_count > 3) ? Preconditions.checkNibbleRegister(register4) : 0;
        this.register5 = (register_count > 4) ? Preconditions.checkNibbleRegister(register5) : 0;
        this.reference1 = ReferenceType.validate(getReferenceType1(), reference1);
    }

    public static Instruction35c35mi35ms of(Opcode opcode, int register_count, int register1,
                                            int register2, int register3, int register4,
                                            int register5, Object reference1) {
        return new Instruction35c35mi35ms(opcode, register_count,
                register1, register2, register3, register4, register5, reference1);
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

    @Override
    public int hashCode() {
        return Objects.hash(getOpcode(), getRegisterCount(),
                getRegister1(), getRegister2(), getRegister3(),
                getRegister4(), getRegister5(), getReference1());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Instruction35c35mi35ms other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getRegisterCount() == other.getRegisterCount()
                && getRegister1() == other.getRegister1()
                && getRegister2() == other.getRegister2()
                && getRegister3() == other.getRegister3()
                && getRegister4() == other.getRegister4()
                && getRegister5() == other.getRegister5()
                && Objects.equals(getReference1(), other.getReference1());
    }
}
