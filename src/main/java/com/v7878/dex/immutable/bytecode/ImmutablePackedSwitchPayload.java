package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Opcode.PACKED_SWITCH_PAYLOAD;

import com.v7878.dex.base.bytecode.BasePackedSwitchPayload;
import com.v7878.dex.iface.bytecode.SwitchElement;
import com.v7878.dex.iface.bytecode.formats.PackedSwitchPayload;
import com.v7878.dex.util.ItemConverter;
import com.v7878.dex.util.Preconditions;

import java.util.List;

public class ImmutablePackedSwitchPayload extends BasePackedSwitchPayload implements ImmutableInstruction {
    private final List<? extends ImmutableSwitchElement> elements;

    protected ImmutablePackedSwitchPayload(Iterable<? extends SwitchElement> elements) {
        super(PACKED_SWITCH_PAYLOAD);
        this.elements = Preconditions.checkSequentialOrderedKeys(
                ItemConverter.toList(ImmutableSwitchElement::of,
                        value -> value instanceof ImmutableSwitchElement, elements));
    }

    public static ImmutablePackedSwitchPayload of(Iterable<? extends SwitchElement> elements) {
        return new ImmutablePackedSwitchPayload(elements);
    }

    public static ImmutablePackedSwitchPayload of(PackedSwitchPayload other) {
        if (other instanceof ImmutablePackedSwitchPayload immutable) return immutable;
        return new ImmutablePackedSwitchPayload(other.getSwitchElements());
    }

    @Override
    public List<? extends ImmutableSwitchElement> getSwitchElements() {
        return elements;
    }
}