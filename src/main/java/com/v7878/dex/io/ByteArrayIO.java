/*
 * Copyright (c) 2023 Vladimir Kozelkov
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.v7878.dex.io;

import com.v7878.dex.util.Checks;

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

    private ByteArrayIO(ModifiableArray arr) {
        this.arr = arr;
        this.offset = 0;
        this.order = ByteOrder.LITTLE_ENDIAN;
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
    public long size() {
        return arr.size();
    }

    @Override
    public long position() {
        return offset;
    }

    @Override
    public void position(long new_position) {
        Checks.checkRange(new_position, 0, Integer.MAX_VALUE);
        arr.ensureSize((int) new_position);
        offset = (int) new_position;
    }

    @Override
    public ByteArrayIO duplicate() {
        return new ByteArrayIO(arr);
    }
}
