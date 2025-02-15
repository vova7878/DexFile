package com.v7878.dex.io;

import static com.v7878.dex.util.AlignmentUtils.isAligned;
import static com.v7878.dex.util.AlignmentUtils.roundUp;

import java.nio.ByteOrder;

class OffsetIO implements RandomIO {

    private final RandomIO delegate;
    private final int offset;

    OffsetIO(Void ignored, RandomIO delegate, int offset) {
        this.delegate = delegate.duplicate();
        this.offset = offset;
    }

    OffsetIO(RandomIO delegate, int offset) {
        this.delegate = delegate.duplicateAt(offset);
        this.offset = offset;
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
    public int size() {
        return delegate.size() - offset;
    }

    @Override
    public int position() {
        return delegate.position() - offset;
    }

    @Override
    public void position(int new_position) {
        delegate.position(Math.addExact(new_position, offset));
    }

    @Override
    public RandomIO duplicate() {
        return new OffsetIO(null, delegate, offset);
    }
}

public interface RandomIO extends RandomInput, RandomOutput {

    default boolean isBigEndian() {
        return getByteOrder() == ByteOrder.BIG_ENDIAN;
    }

    default void addPosition(int delta) {
        position(Math.addExact(position(), delta));
    }

    default void alignPosition(int alignment) {
        position(roundUp(position(), alignment));
    }

    default void requireAlignment(int alignment) {
        int pos = position();
        if (!isAligned(pos, alignment)) {
            throw new IllegalStateException("Position " + pos + " not aligned by " + alignment);
        }
    }

    @Override
    RandomIO duplicate();

    default RandomIO duplicate(int offset) {
        RandomIO out = duplicate();
        out.addPosition(offset);
        return out;
    }

    default RandomIO duplicateAt(int offset) {
        RandomIO out = duplicate();
        out.position(offset);
        return out;
    }

    default RandomIO slice() {
        return sliceAt(position());
    }

    default RandomIO slice(int offset) {
        return sliceAt(Math.addExact(position(), offset));
    }

    default RandomIO sliceAt(int offset) {
        return new OffsetIO(this, offset);
    }
}
