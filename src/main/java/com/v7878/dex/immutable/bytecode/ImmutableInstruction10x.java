package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Format.Format10x;

import com.v7878.dex.Opcode;
import com.v7878.dex.base.bytecode.BaseInstruction10x;
import com.v7878.dex.iface.bytecode.formats.Instruction10x;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public class ImmutableInstruction10x extends BaseInstruction10x implements ImmutableInstruction {
    protected ImmutableInstruction10x(Opcode opcode) {
        super(Preconditions.checkFormat(opcode, Format10x));
    }

    public static ImmutableInstruction10x of(Opcode opcode) {
        return new ImmutableInstruction10x(opcode);
    }

    public static ImmutableInstruction10x of(Instruction10x other) {
        if (other instanceof ImmutableInstruction10x immutable) return immutable;
        return new ImmutableInstruction10x(other.getOpcode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOpcode());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Instruction10x other
                && Objects.equals(getOpcode(), other.getOpcode());
    }
}
