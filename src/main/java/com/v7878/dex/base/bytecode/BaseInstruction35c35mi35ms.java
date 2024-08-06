package com.v7878.dex.base.bytecode;

import static com.v7878.dex.Format.Format35c35mi35ms;

import com.v7878.dex.Opcode;
import com.v7878.dex.iface.bytecode.formats.Instruction35c35mi35ms;

import java.util.Objects;

public abstract class BaseInstruction35c35mi35ms implements Instruction35c35mi35ms {
    private final Opcode opcode;

    public BaseInstruction35c35mi35ms(Opcode opcode) {
        assert opcode.format() == Format35c35mi35ms;
        this.opcode = opcode;
    }

    @Override
    public final Opcode getOpcode() {
        return opcode;
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
