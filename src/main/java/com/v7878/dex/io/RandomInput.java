package com.v7878.dex.io;

import java.nio.ByteOrder;
import java.util.Objects;

class OffsetInput implements RandomInput {
    private final RandomInput delegate;
    private final int offset;

    OffsetInput(RandomInput delegate, int offset) {
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
    public OffsetInput duplicateAt(int position) {
        position = Math.addExact(offset, position);
        return new OffsetInput(delegate.duplicateAt(position), offset);
    }

    @Override
    public OffsetInput sliceAt(int position) {
        position = Math.addExact(offset, position);
        return new OffsetInput(delegate.duplicateAt(position), position);
    }
}

public interface RandomInput extends RandomAccess {
    default void readFully(byte[] arr) {
        readFully(arr, 0, arr.length);
    }

    default void readFully(byte[] arr, int off, int len) {
        Objects.checkFromIndexSize(off, len, arr.length);
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

    default long[] readLongArray(int length) {
        long[] result = new long[length];
        for (int i = 0; i < length; i++) {
            result[i] = readLong();
        }
        return result;
    }

    default boolean readBoolean() {
        return readByte() != 0;
    }

    byte readByte();

    default int readUByte() {
        return readByte() & 0xff;
    }

    default short readShort() {
        return (short) readUShort();
    }

    default int readUShort() {
        int shift = isBigEndian() ? 8 : 0;
        return readUByte() << shift | readUByte() << (8 - shift);
    }

    default char readChar() {
        return (char) readUShort();
    }

    default int readInt() {
        int shift = isBigEndian() ? 16 : 0;
        return readUShort() << shift | readUShort() << (16 - shift);
    }

    default int readSmallUInt() {
        int pos = position();
        int out = readInt();
        if (out < 0) {
            throw new IllegalStateException(
                    "Out of range small uint at position " + pos);
        }
        return out;
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

    default int readSmallULeb128() {
        int pos = position();
        int out = readULeb128();
        if (out < 0) {
            throw new IllegalStateException(
                    "Out of range small uleb128 at position " + pos);
        }
        return out;
    }

    default int readSLeb128() {
        return Leb128.readSignedLeb128(this);
    }

    default String readMUTF8() {
        return MUTF8.readMUTF8(this);
    }

    default void readTo(RandomOutput out, long length) {
        for (int i = 0; i < length; i++) {
            out.writeByte(readByte());
        }
    }

    default void readTo(RandomOutput out) {
        readTo(out, size() - position());
    }

    @Override
    default RandomInput duplicate() {
        return duplicateAt(position());
    }

    @Override
    default RandomInput duplicate(int offset) {
        return duplicateAt(Math.addExact(position(), offset));
    }

    @Override
    RandomInput duplicateAt(int position);

    @Override
    default RandomInput slice() {
        return sliceAt(position());
    }

    @Override
    default RandomInput slice(int offset) {
        return sliceAt(Math.addExact(position(), offset));
    }

    @Override
    default RandomInput sliceAt(int position) {
        return new OffsetInput(this.duplicateAt(position), position);
    }
}
