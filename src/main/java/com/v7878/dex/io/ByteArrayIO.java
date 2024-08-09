package com.v7878.dex.io;

import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Objects;

class ModifiableArray {
    static final int DEFAULT_GROW_FACTOR = 4096; // 4 KiB
    static final int MAX_GROW_FACTOR = 1024 * 1024; // 1 MiB

    private byte[] data;
    private int data_size;
    private int grow_factor = DEFAULT_GROW_FACTOR;

    ModifiableArray(int size) {
        this.data = new byte[size];
        this.data_size = 0;
    }

    ModifiableArray(byte[] new_data) {
        this.data = new_data.clone();
        this.data_size = data.length;
    }

    void setGrowFactor(int new_grow_factor) {
        if (new_grow_factor <= 0) {
            new_grow_factor = DEFAULT_GROW_FACTOR;
        }
        grow_factor = Math.min(new_grow_factor, MAX_GROW_FACTOR);
    }

    int size() {
        return data_size;
    }

    byte[] data() {
        return data;
    }

    byte[] copyData() {
        return Arrays.copyOf(data, data_size);
    }

    void ensureSize(int new_size) {
        if (new_size < 0) {
            throw new IllegalArgumentException("negative size");
        }
        if (new_size > data.length) {
            data = Arrays.copyOf(data, new_size + grow_factor);
        }
        if (new_size > data_size) {
            data_size = new_size;
        }
    }
}

public class ByteArrayIO implements RandomIO {
    private final ModifiableArray arr;
    private ByteOrder order;
    private int offset;

    private ByteArrayIO(ModifiableArray arr, int offset, ByteOrder order) {
        this.arr = arr;
        this.offset = offset;
        this.order = order;
    }

    public ByteArrayIO(byte[] data) {
        this(new ModifiableArray(data), 0, ByteOrder.LITTLE_ENDIAN);
    }

    public ByteArrayIO(int size) {
        this(new ModifiableArray(size), 0, ByteOrder.LITTLE_ENDIAN);
    }

    public ByteArrayIO() {
        this(0);
    }

    public ByteOrder getByteOrder() {
        return order;
    }

    public void setByteOrder(ByteOrder order) {
        this.order = Objects.requireNonNull(order);
    }

    public void setGrowFactor(int new_grow_factor) {
        arr.setGrowFactor(new_grow_factor);
    }

    @Override
    public void writeByte(int value) {
        int index = offset;
        position(index + 1);
        arr.data()[index] = (byte) value;
    }

    @Override
    public byte readByte() {
        int index = offset;
        position(index + 1);
        return arr.data()[index];
    }

    public byte[] toByteArray() {
        return arr.copyData();
    }

    @Override
    public int size() {
        return arr.size();
    }

    @Override
    public int position() {
        return offset;
    }

    @Override
    public void position(int new_position) {
        arr.ensureSize(new_position);
        offset = new_position;
    }

    @Override
    public ByteArrayIO duplicate() {
        return new ByteArrayIO(arr, offset, order);
    }
}
