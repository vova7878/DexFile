package com.v7878.dex.base.bytecode;

import static com.v7878.dex.Format.Format45cc;

import com.v7878.dex.Opcode;
import com.v7878.dex.iface.bytecode.formats.Instruction45cc;

import java.util.Objects;

public abstract class BaseInstruction45cc implements Instruction45cc {
    private final Opcode opcode;

    public BaseInstruction45cc(Opcode opcode) {
        assert opcode.format() == Format45cc;
        this.opcode = opcode;
    }

    @Override
    public final Opcode getOpcode() {
        return opcode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOpcode(), getRegisterCount(), getRegister1(), getRegister2(),
                getRegister3(), getRegister4(), getRegister5(), getReference1(), getReference2());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Instruction45cc other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getRegisterCount() == other.getRegisterCount()
                && getRegister1() == other.getRegister1()
                && getRegister2() == other.getRegister2()
                && getRegister3() == other.getRegister3()
                && getRegister4() == other.getRegister4()
                && getRegister5() == other.getRegister5()
                && Objects.equals(getReference1(), other.getReference1())
                && Objects.equals(getReference2(), other.getReference2());
    }
}
