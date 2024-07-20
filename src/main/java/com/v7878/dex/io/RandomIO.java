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

class OffsetIO implements RandomIO {

    private final RandomIO delegate;
    private final long offset;

    OffsetIO(RandomIO delegate, long offset) {
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
    public RandomIO duplicate() {
        return new OffsetIO(delegate, offset);
    }
}

public interface RandomIO extends RandomInput, RandomOutput {

    default boolean isBigEndian() {
        return getByteOrder() == ByteOrder.BIG_ENDIAN;
    }

    default void addPosition(long delta) {
        position(Math.addExact(position(), delta));
    }

    default void alignPosition(long alignment) {
        position(roundUpL(position(), alignment));
    }

    default void requireAlignment(int alignment) {
        long pos = position();
        if (!isAlignedL(pos, alignment)) {
            throw new IllegalStateException("position " + pos + " not aligned by " + alignment);
        }
    }

    @Override
    RandomIO duplicate();

    @Override
    default RandomIO duplicate(long offset) {
        RandomIO out = duplicate();
        out.addPosition(offset);
        return out;
    }

    default RandomIO slice() {
        return slice(position());
    }

    default RandomIO slice(long offset) {
        return new OffsetIO(this, offset);
    }

    @Override
    default void close() {
    }
}
