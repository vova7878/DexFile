package com.v7878.dex.base.bytecode;

import static com.v7878.dex.Format.Format3rc3rmi3rms;

import com.v7878.dex.Opcode;
import com.v7878.dex.iface.bytecode.formats.Instruction3rc3rmi3rms;

import java.util.Objects;

public abstract class BaseInstruction3rc3rmi3rms implements Instruction3rc3rmi3rms {
    private final Opcode opcode;

    public BaseInstruction3rc3rmi3rms(Opcode opcode) {
        assert opcode.format() == Format3rc3rmi3rms;
        this.opcode = opcode;
    }

    @Override
    public final Opcode getOpcode() {
        return opcode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOpcode(), getRegisterCount(), getStartRegister(), getReference1());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Instruction3rc3rmi3rms other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getRegisterCount() == other.getRegisterCount()
                && getStartRegister() == other.getStartRegister()
                && Objects.equals(getReference1(), other.getReference1());
    }
}
