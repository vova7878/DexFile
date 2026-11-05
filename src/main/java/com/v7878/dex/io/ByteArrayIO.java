package com.v7878.dex.io;

import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Objects;

class ModifiableArray {
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    private byte[] data;
    private int data_size;

    ModifiableArray(int size) {
        this.data = new byte[size];
        this.data_size = 0;
    }

    ModifiableArray(byte[] new_data) {
        this.data = new_data.clone();
        this.data_size = data.length;
    }

    int size() {
        return data_size;
    }

    byte[] data() {
        return data;
    }

    byte[] copyData(int start) {
        return Arrays.copyOfRange(data, start, data_size);
    }

    void ensureSize(int new_size) {
        if (new_size < 0) {
            throw new IllegalArgumentException("negative size: " + new_size);
        }
        if (new_size > data.length) {
            // Grow geometrically
            long grown = Math.max((long) data.length * 2, new_size);
            int capacity = (int) Math.min(grown, MAX_ARRAY_SIZE);
            if (capacity < new_size) {
                throw new OutOfMemoryError(
                        "Required array size too large: " + new_size);
            }
            data = Arrays.copyOf(data, capacity);
        }
        if (new_size > data_size) {
            data_size = new_size;
        }
    }
}

public class ByteArrayIO implements RandomIO {
    private static final int DEFAULT_SIZE = 4096; // 4 KiB
    private final ModifiableArray arr;
    private ByteOrder order;
    private int start;
    private int offset;

    private ByteArrayIO(ModifiableArray arr, int start, int offset, ByteOrder order) {
        this.arr = arr;
        this.start = start;
        this.offset = offset;
        this.order = order;
    }

    public ByteArrayIO(byte[] data) {
        this(new ModifiableArray(data), 0, 0, ByteOrder.LITTLE_ENDIAN);
    }

    public ByteArrayIO(int size) {
        this(new ModifiableArray(size), 0, 0, ByteOrder.LITTLE_ENDIAN);
    }

    public ByteArrayIO() {
        this(DEFAULT_SIZE);
    }

    public ByteOrder getByteOrder() {
        return order;
    }

    public void setByteOrder(ByteOrder order) {
        this.order = Objects.requireNonNull(order);
    }

    @Override
    public void writeByte(int value) {
        int index = offset;
        position(index + 1);
        arr.data()[start + index] = (byte) value;
    }

    @Override
    public byte readByte() {
        int index = offset;
        position(index + 1);
        return arr.data()[start + index];
    }

    /**
     * Returns the written data between the marked start and the current size.
     */
    public byte[] toByteArray() {
        return arr.copyData(start);
    }

    @Override
    public int size() {
        return arr.size() - start;
    }

    @Override
    public int position() {
        return offset;
    }

    @Override
    public void position(int new_position) {
        arr.ensureSize(start + new_position);
        offset = new_position;
    }

    @Override
    public ByteArrayIO duplicateAt(int new_position) {
        arr.ensureSize(start + new_position);
        return new ByteArrayIO(arr, start, new_position, order);
    }

    @Override
    public RandomIO markAsStart() {
        this.start += offset;
        this.offset = 0;
        return this;
    }
}
