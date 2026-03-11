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

public final class SparseSwitchPayload extends Instruction implements SwitchPayloadInstruction {
    private final NavigableSet<SwitchElement> elements;

    private SparseSwitchPayload(Opcode opcode, NavigableSet<SwitchElement> elements) {
        super(Preconditions.checkFormat(opcode,
                Format.SparseSwitchPayload, Format.LegacySparseSwitchPayload));
        this.elements = Objects.requireNonNull(elements);
    }

    @Internal
    public static SparseSwitchPayload raw(Opcode opcode, NavigableSet<SwitchElement> elements) {
        return new SparseSwitchPayload(opcode, elements);
    }

    public static SparseSwitchPayload of(Opcode opcode, Iterable<SwitchElement> elements) {
        return new SparseSwitchPayload(opcode, Converter.toNavigableSet(elements));
    }

    @Override
    public NavigableSet<SwitchElement> getSwitchElements() {
        return elements;
    }

    @Override
    public int getUnitCount() {
        if (getOpcode().format() == Format.LegacySparseSwitchPayload) {
            return Preconditions.getLegacySparseSwitchPayloadUnitCount(getSwitchElements().size());
        }
        return Preconditions.getSparseSwitchPayloadUnitCount(getSwitchElements().size());
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
