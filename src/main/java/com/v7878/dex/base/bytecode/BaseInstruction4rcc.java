package com.v7878.dex.base.bytecode;

import static com.v7878.dex.Format.Format4rcc;

import com.v7878.dex.Opcode;
import com.v7878.dex.iface.bytecode.formats.Instruction4rcc;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public abstract class BaseInstruction4rcc extends BaseInstruction implements Instruction4rcc {
    public BaseInstruction4rcc(Opcode opcode) {
        super(Preconditions.checkFormat(opcode, Format4rcc));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOpcode(), getRegisterCount(),
                getStartRegister(), getReference1(), getReference2());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Instruction4rcc other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getRegisterCount() == other.getRegisterCount()
                && getStartRegister() == other.getStartRegister()
                && Objects.equals(getReference1(), other.getReference1())
                && Objects.equals(getReference2(), other.getReference2());
    }
}
