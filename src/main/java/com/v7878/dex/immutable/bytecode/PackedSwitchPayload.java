package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Opcode.PACKED_SWITCH_PAYLOAD;

import com.v7878.dex.Format;
import com.v7878.dex.Internal;
import com.v7878.dex.immutable.bytecode.iface.SwitchPayloadInstruction;
import com.v7878.dex.util.Converter;
import com.v7878.dex.util.Preconditions;

import java.util.NavigableSet;
import java.util.Objects;
import java.util.stream.Collectors;

public final class PackedSwitchPayload extends Instruction implements SwitchPayloadInstruction {
    private final NavigableSet<SwitchElement> elements;

    private PackedSwitchPayload(NavigableSet<SwitchElement> elements) {
        super(Preconditions.checkFormat(PACKED_SWITCH_PAYLOAD, Format.PackedSwitchPayload));
        this.elements = Objects.requireNonNull(elements);
    }

    @Internal
    public static PackedSwitchPayload raw(NavigableSet<SwitchElement> elements) {
        return new PackedSwitchPayload(elements);
    }

    public static PackedSwitchPayload of(Iterable<SwitchElement> elements) {
        var set = Converter.toNavigableSet(elements);
        return new PackedSwitchPayload(Preconditions.checkSequentialOrderedKeys(set));
    }

    @Override
    public NavigableSet<SwitchElement> getSwitchElements() {
        return elements;
    }

    @Override
    public int getUnitCount() {
        return Preconditions.getPackedSwitchPayloadUnitCount(getSwitchElements().size());
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

    @Override
    public String toString() {
        var builder = new StringBuilder();
        builder.append(getName());
        builder.append("{");
        var cases = elements;
        if (!cases.isEmpty()) {
            builder.append(cases.stream().map(Objects::toString)
                    .collect(Collectors.joining("\t\n", "\n", "\n")));
        }
        builder.append("}");
        return builder.toString();
    }
}
