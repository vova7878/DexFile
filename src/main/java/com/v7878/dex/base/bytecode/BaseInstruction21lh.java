package com.v7878.dex.base.bytecode;

import static com.v7878.dex.Format.Format21lh;

import com.v7878.dex.Opcode;
import com.v7878.dex.iface.bytecode.formats.Instruction21lh;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public abstract class BaseInstruction21lh extends BaseInstruction implements Instruction21lh {
    public BaseInstruction21lh(Opcode opcode) {
        super(Preconditions.checkFormat(opcode, Format21lh));
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
