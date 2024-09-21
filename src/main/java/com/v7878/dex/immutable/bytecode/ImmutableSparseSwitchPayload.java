package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Opcode.SPARSE_SWITCH_PAYLOAD;

import com.v7878.dex.Format;
import com.v7878.dex.base.bytecode.BaseSparseSwitchPayload;
import com.v7878.dex.iface.bytecode.SwitchElement;
import com.v7878.dex.iface.bytecode.formats.SparseSwitchPayload;
import com.v7878.dex.util.ItemConverter;
import com.v7878.dex.util.Preconditions;

import java.util.List;
import java.util.Objects;

public class ImmutableSparseSwitchPayload extends BaseSparseSwitchPayload implements ImmutableInstruction {
    private final List<? extends ImmutableSwitchElement> elements;

    protected ImmutableSparseSwitchPayload(Iterable<? extends SwitchElement> elements) {
        super(Preconditions.checkFormat(SPARSE_SWITCH_PAYLOAD, Format.SparseSwitchPayload));
        this.elements = ItemConverter.toList(ImmutableSwitchElement::of,
                value -> value instanceof ImmutableSwitchElement, elements);
    }

    public static ImmutableSparseSwitchPayload of(Iterable<? extends SwitchElement> elements) {
        return new ImmutableSparseSwitchPayload(elements);
    }

    public static ImmutableSparseSwitchPayload of(SparseSwitchPayload other) {
        if (other instanceof ImmutableSparseSwitchPayload immutable) return immutable;
        return new ImmutableSparseSwitchPayload(other.getSwitchElements());
    }

    @Override
    public List<? extends ImmutableSwitchElement> getSwitchElements() {
        return elements;
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
