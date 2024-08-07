package com.v7878.dex.base.bytecode;

import static com.v7878.dex.Format.Format35c35mi35ms;

import com.v7878.dex.Opcode;
import com.v7878.dex.iface.bytecode.formats.Instruction35c35mi35ms;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public abstract class BaseInstruction35c35mi35ms extends BaseInstruction implements Instruction35c35mi35ms {
    public BaseInstruction35c35mi35ms(Opcode opcode) {
        super(Preconditions.checkFormat(opcode, Format35c35mi35ms));
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
