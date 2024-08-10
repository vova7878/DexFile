package com.v7878.dex.io;

import java.nio.ByteOrder;
import java.util.Objects;

public class ByteArrayInput implements RandomInput {
    private final byte[] array;
    private ByteOrder order;
    private int offset;

    private ByteArrayInput(byte[] array, int offset, ByteOrder order) {
        this.array = array;
        this.offset = offset;
        this.order = order;
    }

    public ByteArrayInput(byte[] array) {
        this.array = Objects.requireNonNull(array);
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
        return array[index];
    }

    @Override
    public int size() {
        return array.length;
    }

    @Override
    public int position() {
        return offset;
    }

    @Override
    public void position(int new_position) {
        Objects.checkIndex(new_position, array.length + 1);
        offset = new_position;
    }

    @Override
    public RandomInput duplicate() {
        return new ByteArrayInput(array, offset, order);
    }
}
