package com.v7878.dex.io;

import java.nio.ByteOrder;
import java.util.Objects;

public class ByteArrayInput implements RandomInput {
    private final byte[] array;
    private ByteOrder order;
    private int start;
    private int offset;

    private ByteArrayInput(byte[] array, int start, int offset, ByteOrder order) {
        this.array = array;
        this.start = start;
        this.offset = offset;
        this.order = order;
    }

    public ByteArrayInput(byte[] array) {
        this(array, 0, 0, ByteOrder.LITTLE_ENDIAN);
    }

    public ByteOrder getByteOrder() {
        return order;
    }

    public void setByteOrder(ByteOrder order) {
        this.order = Objects.requireNonNull(order);
    }

    @Override
    public byte readByte() {
        int index = offset;
        position(index + 1);
        return array[start + index];
    }

    @Override
    public int size() {
        return array.length - start;
    }

    @Override
    public int position() {
        return offset;
    }

    private int checkPosition(int position) {
        Objects.checkIndex(start + position, array.length + 1);
        return position;
    }

    @Override
    public void position(int position) {
        offset = checkPosition(position);
    }

    @Override
    public RandomInput duplicateAt(int position) {
        return new ByteArrayInput(array, start, checkPosition(position), order);
    }

    @Override
    public RandomInput markAsStart() {
        this.start += offset;
        this.offset = 0;
        return this;
    }
}
