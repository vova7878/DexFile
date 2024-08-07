package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Opcode.ARRAY_PAYLOAD;

import com.v7878.dex.base.bytecode.BaseArrayPayload;
import com.v7878.dex.iface.bytecode.formats.ArrayPayload;
import com.v7878.dex.util.ItemConverter;
import com.v7878.dex.util.Preconditions;

import java.util.List;
import java.util.Objects;

public class ImmutableArrayPayload extends BaseArrayPayload implements ImmutableInstruction {
    private final int element_width;
    private final List<? extends Number> elements;

    protected ImmutableArrayPayload(int element_width, Iterable<? extends Number> elements) {
        super(ARRAY_PAYLOAD);
        this.element_width = Preconditions.checkArrayPayloadElementWidth(element_width);
        this.elements = Preconditions.checkArrayPayloadElements(element_width,
                ItemConverter.toList(Objects::requireNonNull, Objects::nonNull, elements));
    }

    public static ImmutableArrayPayload of(int element_width, Iterable<? extends Number> elements) {
        return new ImmutableArrayPayload(element_width, elements);
    }

    public static ImmutableArrayPayload of(ArrayPayload other) {
        if (other instanceof ImmutableArrayPayload immutable) return immutable;
        return new ImmutableArrayPayload(other.getElementWidth(), other.getArrayElements());
    }

    @Override
    public int getElementWidth() {
        return element_width;
    }

    @Override
    public List<? extends Number> getArrayElements() {
        return elements;
    }
}
