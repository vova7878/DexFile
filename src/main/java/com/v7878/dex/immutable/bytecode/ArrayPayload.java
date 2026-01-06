package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Opcode.ARRAY_PAYLOAD;

import com.v7878.dex.Format;
import com.v7878.dex.Internal;
import com.v7878.dex.immutable.bytecode.iface.ArrayPayloadInstruction;
import com.v7878.dex.util.Converter;
import com.v7878.dex.util.Preconditions;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class ArrayPayload extends Instruction implements ArrayPayloadInstruction {
    private final int element_width;
    private final List<? extends Number> elements;

    private ArrayPayload(int element_width, List<? extends Number> elements) {
        super(Preconditions.checkFormat(ARRAY_PAYLOAD, Format.ArrayPayload));
        this.elements = Objects.requireNonNull(elements);
        assert element_width == 1 || element_width == 2 || element_width == 4 || element_width == 8;
        this.element_width = element_width;
    }

    @Internal
    public static ArrayPayload raw(int element_width, List<? extends Number> elements) {
        return new ArrayPayload(element_width, elements);
    }

    public static ArrayPayload of(int element_width, Iterable<? extends Number> elements) {
        return new ArrayPayload(element_width, Preconditions.checkArrayPayloadElements(
                element_width, Converter.toList(elements)));
    }

    @Override
    public int getElementWidth() {
        return element_width;
    }

    @Override
    public List<? extends Number> getArrayElements() {
        return elements;
    }

    @Override
    public int getUnitCount() {
        return Preconditions.getArrayPayloadUnitCount(element_width, elements.size());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOpcode(), getElementWidth(), getArrayElements());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof ArrayPayloadInstruction other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getElementWidth() == other.getElementWidth()
                && Objects.equals(getArrayElements(), other.getArrayElements());
    }

    @Override
    public String toString() {
        var builder = new StringBuilder();
        builder.append(getName());
        builder.append(" ");
        builder.append(element_width);
        builder.append(" {");
        var cases = elements;
        if (!cases.isEmpty()) {
            builder.append(cases.stream().map(Objects::toString)
                    .collect(Collectors.joining("\t\n,", "\n", "\n")));
        }
        builder.append("}");
        return builder.toString();
    }
}
