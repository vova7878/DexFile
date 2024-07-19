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

import com.v7878.misc.Checks;

import java.nio.ByteOrder;
import java.util.Objects;

class OffsetInput implements RandomInput {

    private final RandomInput delegate;
    private final long offset;

    OffsetInput(RandomInput delegate, long offset) {
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
    public byte readByte() {
        return delegate.readByte();
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
    public long position(long new_position) {
        return delegate.position(Math.addExact(new_position, offset)) - offset;
    }

    @Override
    public RandomInput duplicate() {
        return new OffsetInput(delegate.duplicate(offset), offset);
    }
}

public interface RandomInput extends AutoCloseable {

    ByteOrder getByteOrder();

    default boolean isBigEndian() {
        return getByteOrder() == ByteOrder.BIG_ENDIAN;
    }

    void setByteOrder(ByteOrder order);

    default void readFully(byte[] arr) {
        readFully(arr, 0, arr.length);
    }

    default void readFully(byte[] arr, int off, int len) {
        Objects.requireNonNull(arr);
        Checks.checkFromIndexSize(off, len, arr.length);
        if (len == 0) {
            return;
        }
        for (int i = 0; i < len; i++) {
            arr[i + off] = readByte();
        }
    }

    default byte[] readByteArray(int length) {
        byte[] result = new byte[length];
        readFully(result);
        return result;
    }

    default short[] readShortArray(int length) {
        short[] result = new short[length];
        for (int i = 0; i < length; i++) {
            result[i] = readShort();
        }
        return result;
    }

    default int[] readIntArray(int length) {
        int[] result = new int[length];
        for (int i = 0; i < length; i++) {
            result[i] = readInt();
        }
        return result;
    }

    default boolean readBoolean() {
        return readByte() != 0;
    }

    byte readByte();

    default int readUnsignedByte() {
        return readByte() & 0xff;
    }

    default short readShort() {
        return (short) readUnsignedShort();
    }

    default int readUnsignedShort() {
        int shift = isBigEndian() ? 8 : 0;
        return readUnsignedByte() << shift | readUnsignedByte() << (8 - shift);
    }

    default char readChar() {
        return (char) readUnsignedShort();
    }

    default int readInt() {
        int shift = isBigEndian() ? 16 : 0;
        return readUnsignedShort() << shift | readUnsignedShort() << (16 - shift);
    }

    default long readLong() {
        int shift = isBigEndian() ? 32 : 0;
        return (readInt() & 0xffffffffL) << shift | (readInt() & 0xffffffffL) << (32 - shift);
    }

    default float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    default double readDouble() {
        return Double.longBitsToDouble(readLong());
    }

    default int readULeb128() {
        return Leb128.readUnsignedLeb128(this);
    }

    default int readSLeb128() {
        return Leb128.readSignedLeb128(this);
    }

    default String readMUTF8() {
        return MUTF8.readMUTF8(this);
    }

    long size();

    long position();

    long position(long new_position);

    default long addPosition(long delta) {
        return position(Math.addExact(position(), delta));
    }

    default long alignPosition(long alignment) {
        return position(roundUpL(position(), alignment));
    }

    default void requireAlignment(int alignment) {
        long pos = position();
        if (!isAlignedL(pos, alignment)) {
            throw new IllegalStateException("position " + pos + " not aligned by " + alignment);
        }
    }

    RandomInput duplicate();

    default RandomInput duplicate(long offset) {
        RandomInput out = duplicate();
        out.addPosition(offset);
        return out;
    }

    default RandomInput slice() {
        return slice(position());
    }

    default RandomInput slice(long offset) {
        return new OffsetInput(this, offset);
    }

    @Override
    default void close() {
    }
}
