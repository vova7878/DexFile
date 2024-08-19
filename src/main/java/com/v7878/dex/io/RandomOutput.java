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

import static com.v7878.misc.Math.isAlignedL;
import static com.v7878.misc.Math.roundUpL;

import java.nio.ByteOrder;
import java.util.Objects;


class OffsetOutput implements RandomOutput {

    private final RandomOutput delegate;
    private final long offset;

    OffsetOutput(RandomOutput delegate, long offset) {
        this.delegate = delegate.duplicate();
        this.offset = offset;
        this.delegate.position(offset);
    }

    @Override
    public ByteOrder getByteOrder() {
        return delegate.getByteOrder();
    }

    @Override
    public void setByteOrder(ByteOrder order) {
        delegate.setByteOrder(order);
    }

    @Override
    public void writeByte(int value) {
        delegate.writeByte(value);
    }

    @Override
    public long size() {
        return delegate.size() - offset;
    }

    @Override
    public long position() {
        return delegate.position() - offset;
    }

    @Override
    public void position(long new_position) {
        delegate.position(Math.addExact(new_position, offset));
    }

    @Override
    public RandomOutput duplicate() {
        return new OffsetOutput(delegate, offset);
    }
}

public interface RandomOutput extends AutoCloseable {

    ByteOrder getByteOrder();

    default boolean isBigEndian() {
        return getByteOrder() == ByteOrder.BIG_ENDIAN;
    }

    void setByteOrder(ByteOrder order);

    default void writeByteArray(byte[] arr) {
        writeByteArray(arr, 0, arr.length);
    }

    default void writeByteArray(byte[] arr, int off, int len) {
        Objects.requireNonNull(arr);
        Objects.checkFromIndexSize(off, len, arr.length);
        if (len == 0) {
            return;
        }
        for (int i = 0; i < len; i++) {
            writeByte(arr[i + off]);
        }
    }

    default void writeShortArray(short[] shorts) {
        for (short value : shorts) {
            writeShort(value);
        }
    }

    default void writeIntArray(int[] ints) {
        for (int value : ints) {
            writeInt(value);
        }
    }

    default void writeBoolean(boolean value) {
        writeByte(value ? 1 : 0);
    }

    void writeByte(int value);

    default void writeShort(int value) {
        int shift = isBigEndian() ? 8 : 0;
        writeByte(value >> shift);
        writeByte(value >> (8 - shift));
    }

    default void writeChar(int value) {
        writeShort(value);
    }

    default void writeInt(int value) {
        int shift = isBigEndian() ? 16 : 0;
        writeShort(value >> shift);
        writeShort(value >> (16 - shift));
    }

    default void writeLong(long value) {
        int shift = isBigEndian() ? 32 : 0;
        writeInt((int) (value >> shift));
        writeInt((int) (value >> (32 - shift)));
    }

    default void writeFloat(float value) {
        writeInt(Float.floatToRawIntBits(value));
    }

    default void writeDouble(double value) {
        writeLong(Double.doubleToRawLongBits(value));
    }

    default void writeULeb128(int value) {
        Leb128.writeUnsignedLeb128(this, value);
    }

    default void writeSLeb128(int value) {
        Leb128.writeSignedLeb128(this, value);
    }

    default void writeMUtf8(String value) {
        MUTF8.writeMUTF8(this, value);
    }

    default void writeFrom(RandomInput in, long length) {
        for (int i = 0; i < length; i++) {
            writeByte(in.readByte());
        }
    }

    default void writeFrom(RandomInput in) {
        writeFrom(in, size() - position());
    }

    long size();

    long position();

    void position(long new_position);

    default void addPosition(long delta) {
        position(Math.addExact(position(), delta));
    }

    default void alignPosition(long alignment) {
        position(roundUpL(position(), alignment));
    }

    default void fillZerosToAlignment(long alignment) {
        long old_position = position();
        long new_position = roundUpL(old_position, alignment);
        for (long i = 0; i < new_position - old_position; i++) {
            writeByte(0);
        }
    }

    default void requireAlignment(int alignment) {
        long pos = position();
        if (!isAlignedL(pos, alignment)) {
            throw new IllegalStateException("position " + pos + " not aligned by " + alignment);
        }
    }

    RandomOutput duplicate();

    default RandomOutput duplicate(long offset) {
        RandomOutput out = duplicate();
        out.addPosition(offset);
        return out;
    }

    default RandomOutput slice() {
        return slice(position());
    }

    default RandomOutput slice(long offset) {
        return new OffsetOutput(this, offset);
    }

    @Override
    default void close() {
    }
}
