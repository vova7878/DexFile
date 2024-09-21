package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Opcode.SPARSE_SWITCH_PAYLOAD;

import com.v7878.dex.Format;
import com.v7878.dex.immutable.bytecode.iface.SwitchPayloadInstruction;
import com.v7878.dex.util.ItemConverter;
import com.v7878.dex.util.Preconditions;

import java.util.List;
import java.util.Objects;

public class SparseSwitchPayload extends Instruction implements SwitchPayloadInstruction {
    private final List<? extends SwitchElement> elements;

    protected SparseSwitchPayload(Iterable<? extends SwitchElement> elements) {
        super(Preconditions.checkFormat(SPARSE_SWITCH_PAYLOAD, Format.SparseSwitchPayload));
        this.elements = ItemConverter.toList(elements);
    }

    public static SparseSwitchPayload of(Iterable<? extends SwitchElement> elements) {
        return new SparseSwitchPayload(elements);
    }

    @Override
    public List<? extends SwitchElement> getSwitchElements() {
        return elements;
    }

    @Override
    public int getUnitCount() {
        return getSwitchElements().size() * 4 + 2;
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
