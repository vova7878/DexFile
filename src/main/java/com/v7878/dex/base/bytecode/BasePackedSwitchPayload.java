package com.v7878.dex.base.bytecode;

import static com.v7878.dex.Format.PackedSwitchPayload;

import com.v7878.dex.Opcode;
import com.v7878.dex.iface.bytecode.formats.PackedSwitchPayload;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public abstract class BasePackedSwitchPayload extends BaseInstruction implements PackedSwitchPayload {
    public BasePackedSwitchPayload(Opcode opcode) {
        super(Preconditions.checkFormat(opcode, PackedSwitchPayload));
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
