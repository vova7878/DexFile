package com.v7878.dex.base.bytecode;

import static com.v7878.dex.Format.PackedSwitchPayload;

import com.v7878.dex.Opcode;
import com.v7878.dex.iface.bytecode.formats.PackedSwitchPayload;

import java.util.Objects;

public abstract class BasePackedSwitchPayload implements PackedSwitchPayload {
    private final Opcode opcode;

    public BasePackedSwitchPayload(Opcode opcode) {
        assert opcode.format() == PackedSwitchPayload;
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
        return obj instanceof PackedSwitchPayload other
                && Objects.equals(getOpcode(), other.getOpcode())
                && Objects.equals(getSwitchElements(), other.getSwitchElements());
    }
}
