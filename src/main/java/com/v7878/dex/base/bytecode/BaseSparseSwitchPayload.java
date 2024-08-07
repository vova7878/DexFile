package com.v7878.dex.base.bytecode;

import static com.v7878.dex.Format.SparseSwitchPayload;

import com.v7878.dex.Opcode;
import com.v7878.dex.iface.bytecode.formats.SparseSwitchPayload;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public abstract class BaseSparseSwitchPayload extends BaseInstruction implements SparseSwitchPayload {
    public BaseSparseSwitchPayload(Opcode opcode) {
        super(Preconditions.checkFormat(opcode, SparseSwitchPayload));
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
