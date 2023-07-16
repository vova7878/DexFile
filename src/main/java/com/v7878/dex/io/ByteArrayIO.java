package com.v7878.dex.io;

import com.v7878.misc.Checks;

import java.util.Arrays;

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
    private int offset;

    private ByteArrayIO(ModifiableArray arr) {
        this.arr = arr;
        this.offset = 0;
    }

    public ByteArrayIO(byte[] data) {
        this(new ModifiableArray(data));
    }

    public ByteArrayIO(int size) {
        this(new ModifiableArray(size));
    }

    public ByteArrayIO() {
        this(0);
    }

    public void setGrowFactor(int new_grow_factor) {
        arr.setGrowFactor(new_grow_factor);
    }

    @Override
    public void writeByte(int value) {
        int index = (int) addPosition(1);
        arr.data()[index] = (byte) value;
    }

    @Override
    public byte readByte() {
        int index = (int) addPosition(1);
        return arr.data()[index];
    }

    public byte[] toByteArray() {
        return arr.copyData();
    }

    @Override
    public long size() {
        return arr.size();
    }

    @Override
    public long position() {
        return offset;
    }

    @Override
    public long position(long new_position) {
        Checks.checkRange(new_position, 0, Integer.MAX_VALUE);
        arr.ensureSize((int) new_position);
        long tmp = offset;
        offset = (int) new_position;
        return tmp;
    }

    @Override
    public ByteArrayIO duplicate() {
        return new ByteArrayIO(arr);
    }
}
