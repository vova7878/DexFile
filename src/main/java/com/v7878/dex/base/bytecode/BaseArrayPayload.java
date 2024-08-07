package com.v7878.dex.base.bytecode;

import static com.v7878.dex.Format.ArrayPayload;

import com.v7878.dex.Opcode;
import com.v7878.dex.iface.bytecode.formats.ArrayPayload;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public abstract class BaseArrayPayload extends BaseInstruction implements ArrayPayload {
    public BaseArrayPayload(Opcode opcode) {
        super(Preconditions.checkFormat(opcode, ArrayPayload));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOpcode(), getElementWidth(), getArrayElements());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof ArrayPayload other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getElementWidth() == other.getElementWidth()
                && Objects.equals(getArrayElements(), other.getArrayElements());
    }
}
