package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Format.MPackedSwitchPayload;
import static com.v7878.dex.Format.MSparseSwitchPayload;
import static com.v7878.dex.Format.PackedSwitchPayload;
import static com.v7878.dex.Format.SparseSwitchPayload;
import static com.v7878.dex.util.Checks.shouldNotReachHere;

import com.v7878.dex.Internal;
import com.v7878.dex.Opcode;
import com.v7878.dex.immutable.bytecode.iface.SwitchPayloadInstruction;
import com.v7878.dex.util.Converter;
import com.v7878.dex.util.Preconditions;

import java.util.NavigableSet;
import java.util.Objects;
import java.util.stream.Collectors;

public final class SwitchPayload extends Instruction implements SwitchPayloadInstruction {
    private final NavigableSet<SwitchElement> elements;

    private SwitchPayload(Opcode opcode, NavigableSet<SwitchElement> elements) {
        super(Preconditions.checkFormat(opcode, "SwitchPayload",
                PackedSwitchPayload, SparseSwitchPayload,
                MPackedSwitchPayload, MSparseSwitchPayload));
        this.elements = Objects.requireNonNull(elements);
    }

    @Internal
    public static SwitchPayload raw(Opcode opcode, NavigableSet<SwitchElement> elements) {
        return new SwitchPayload(opcode, elements);
    }

    public static SwitchPayload of(Opcode opcode, Iterable<SwitchElement> elements) {
        var set = Converter.toNavigableSet(elements);
        return new SwitchPayload(opcode, switch (opcode.format()) {
            case MPackedSwitchPayload, PackedSwitchPayload ->
                    Preconditions.checkSequentialOrderedKeys(set);
            case MSparseSwitchPayload, SparseSwitchPayload -> set;
            default -> throw shouldNotReachHere();
        });
    }

    @Override
    public NavigableSet<SwitchElement> getSwitchElements() {
        return elements;
    }

    @Override
    public int getUnitCount() {
        var size = getSwitchElements().size();
        return switch (getOpcode().format()) {
            case MPackedSwitchPayload -> Preconditions.getLegacyPackedSwitchPayloadUnitCount(size);
            case PackedSwitchPayload -> Preconditions.getPackedSwitchPayloadUnitCount(size);
            case MSparseSwitchPayload -> Preconditions.getLegacySparseSwitchPayloadUnitCount(size);
            case SparseSwitchPayload -> Preconditions.getSparseSwitchPayloadUnitCount(size);
            default -> throw shouldNotReachHere();
        };
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOpcode(), getSwitchElements());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof SwitchPayload other
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
