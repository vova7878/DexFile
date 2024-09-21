package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Opcode.ARRAY_PAYLOAD;

import com.v7878.dex.Format;
import com.v7878.dex.immutable.bytecode.iface.ArrayPayloadInstruction;
import com.v7878.dex.util.ItemConverter;
import com.v7878.dex.util.Preconditions;

import java.util.List;
import java.util.Objects;

public final class ArrayPayload extends Instruction implements ArrayPayloadInstruction {
    private final int element_width;
    private final List<Number> elements;

    private ArrayPayload(int element_width, Iterable<Number> elements) {
        super(Preconditions.checkFormat(ARRAY_PAYLOAD, Format.ArrayPayload));
        this.element_width = Preconditions.checkArrayPayloadElementWidth(element_width);
        this.elements = Preconditions.checkArrayPayloadElements(
                element_width, ItemConverter.toList(elements));
    }

    public static ArrayPayload of(int element_width, Iterable<Number> elements) {
        return new ArrayPayload(element_width, elements);
    }

    @Override
    public int getElementWidth() {
        return element_width;
    }

    @Override
    public List<Number> getArrayElements() {
        return elements;
    }

    @Override
    public int getUnitCount() {
        return (getArrayElements().size() * getElementWidth() + 1) / 2 + 4;
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
}
