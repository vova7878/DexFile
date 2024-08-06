package com.v7878.dex.base.bytecode;

import static com.v7878.dex.Format.Format21ih;

import com.v7878.dex.Opcode;
import com.v7878.dex.iface.bytecode.formats.Instruction21ih;

import java.util.Objects;

public abstract class BaseInstruction21ih implements Instruction21ih {
    private final Opcode opcode;

    public BaseInstruction21ih(Opcode opcode) {
        assert opcode.format() == Format21ih;
        this.opcode = opcode;
    }

    @Override
    public final Opcode getOpcode() {
        return opcode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOpcode(), getRegister1(), getLiteral());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Instruction21ih other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getRegister1() == other.getRegister1()
                && getLiteral() == other.getLiteral();
    }
}
