package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Opcode.SPARSE_SWITCH_PAYLOAD;

import com.v7878.dex.Format;
import com.v7878.dex.Internal;
import com.v7878.dex.immutable.bytecode.iface.SwitchPayloadInstruction;
import com.v7878.dex.util.Converter;
import com.v7878.dex.util.Preconditions;

import java.util.NavigableSet;
import java.util.Objects;

public final class SparseSwitchPayload extends Instruction implements SwitchPayloadInstruction {
    private final NavigableSet<SwitchElement> elements;

    private SparseSwitchPayload(NavigableSet<SwitchElement> elements) {
        super(Preconditions.checkFormat(SPARSE_SWITCH_PAYLOAD, Format.SparseSwitchPayload));
        this.elements = Objects.requireNonNull(elements);
    }

    @Internal
    public static SparseSwitchPayload raw(NavigableSet<SwitchElement> elements) {
        return new SparseSwitchPayload(elements);
    }

    public static SparseSwitchPayload of(Iterable<SwitchElement> elements) {
        return new SparseSwitchPayload(Converter.toNavigableSet(elements));
    }

    @Override
    public NavigableSet<SwitchElement> getSwitchElements() {
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
