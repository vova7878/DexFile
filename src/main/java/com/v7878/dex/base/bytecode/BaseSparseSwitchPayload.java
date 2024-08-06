package com.v7878.dex.base.bytecode;

import static com.v7878.dex.Format.SparseSwitchPayload;

import com.v7878.dex.Opcode;
import com.v7878.dex.iface.bytecode.formats.SparseSwitchPayload;

import java.util.Objects;

public abstract class BaseSparseSwitchPayload implements SparseSwitchPayload {
    private final Opcode opcode;

    public BaseSparseSwitchPayload(Opcode opcode) {
        assert opcode.format() == SparseSwitchPayload;
        this.opcode = opcode;
    }

    @Override
    public final Opcode getOpcode() {
        return opcode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOpcode(), getSwitchElements());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof SparseSwitchPayload other
                && Objects.equals(getOpcode(), other.getOpcode())
                && Objects.equals(getSwitchElements(), other.getSwitchElements());
    }
}
