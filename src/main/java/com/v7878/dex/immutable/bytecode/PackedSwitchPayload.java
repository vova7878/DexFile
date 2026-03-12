package com.v7878.dex.immutable.bytecode;

import com.v7878.dex.Format;
import com.v7878.dex.Internal;
import com.v7878.dex.Opcode;
import com.v7878.dex.immutable.bytecode.iface.SwitchPayloadInstruction;
import com.v7878.dex.util.Converter;
import com.v7878.dex.util.Preconditions;

import java.util.NavigableSet;
import java.util.Objects;
import java.util.stream.Collectors;

public final class PackedSwitchPayload extends Instruction implements SwitchPayloadInstruction {
    private final NavigableSet<SwitchElement> elements;

    private PackedSwitchPayload(Opcode opcode, NavigableSet<SwitchElement> elements) {
        super(Preconditions.checkFormat(opcode,
                Format.PackedSwitchPayload, Format.MPackedSwitchPayload));
        this.elements = Objects.requireNonNull(elements);
    }

    @Internal
    public static PackedSwitchPayload raw(Opcode opcode, NavigableSet<SwitchElement> elements) {
        return new PackedSwitchPayload(opcode, elements);
    }

    public static PackedSwitchPayload of(Opcode opcode, Iterable<SwitchElement> elements) {
        var set = Converter.toNavigableSet(elements);
        return new PackedSwitchPayload(opcode, Preconditions.checkSequentialOrderedKeys(set));
    }

    @Override
    public NavigableSet<SwitchElement> getSwitchElements() {
        return elements;
    }

    @Override
    public int getUnitCount() {
        if (getOpcode().format() == Format.MPackedSwitchPayload) {
            return Preconditions.getLegacyPackedSwitchPayloadUnitCount(getSwitchElements().size());
        }
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
        builder.append(" {");
        var cases = elements;
        if (!cases.isEmpty()) {
            builder.append(cases.stream().map(Objects::toString)
                    .collect(Collectors.joining("\n\t", "\n\t", "\n")));
        }
        builder.append("}");
        return builder.toString();
    }
}
