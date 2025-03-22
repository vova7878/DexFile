package com.v7878.dex.io;

import java.nio.ByteOrder;

class OffsetIO implements RandomIO {
    private final RandomIO delegate;
    private final int offset;

    OffsetIO(RandomIO delegate, int offset) {
        this.delegate = delegate;
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
    public void position(int position) {
        delegate.position(Math.addExact(offset, position));
    }

    @Override
    public OffsetIO duplicateAt(int position) {
        position = Math.addExact(offset, position);
        return new OffsetIO(delegate.duplicateAt(position), offset);
    }

    @Override
    public OffsetIO sliceAt(int position) {
        position = Math.addExact(offset, position);
        return new OffsetIO(delegate.duplicateAt(position), position);
    }
}

public interface RandomIO extends RandomInput, RandomOutput {
    @Override
    default RandomIO duplicate() {
        return duplicateAt(position());
    }

    @Override
    default RandomIO duplicate(int offset) {
        return duplicateAt(Math.addExact(position(), offset));
    }

    @Override
    RandomIO duplicateAt(int position);

    @Override
    default RandomIO slice() {
        return sliceAt(position());
    }

    @Override
    default RandomIO slice(int offset) {
        return sliceAt(Math.addExact(position(), offset));
    }

    default RandomIO sliceAt(int position) {
        return new OffsetIO(this.duplicateAt(position), position);
    }
}
