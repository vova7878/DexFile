package com.v7878.dex.base.bytecode;

import static com.v7878.dex.Format.Format21lh;

import com.v7878.dex.Opcode;
import com.v7878.dex.iface.bytecode.formats.Instruction21lh;

import java.util.Objects;

public abstract class BaseInstruction21lh implements Instruction21lh {
    private final Opcode opcode;

    public BaseInstruction21lh(Opcode opcode) {
        assert opcode.format() == Format21lh;
        this.opcode = opcode;
    }

    @Override
    public final Opcode getOpcode() {
        return opcode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOpcode(), getRegister1(), getWideLiteral());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Instruction21lh other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getRegister1() == other.getRegister1()
                && getWideLiteral() == other.getWideLiteral();
    }
}
