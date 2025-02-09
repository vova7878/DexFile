package com.v7878.dex.io;

import static com.v7878.misc.Math.isAligned;
import static com.v7878.misc.Math.roundUp;

import java.nio.ByteOrder;
import java.util.Objects;

class OffsetInput implements RandomInput {

    private final RandomInput delegate;
    private final int offset;

    OffsetInput(Void ignored, RandomInput delegate, int offset) {
        this.delegate = delegate.duplicate();
        this.offset = offset;
    }

    OffsetInput(RandomInput delegate, int offset) {
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
    public RandomInput duplicate() {
        return new OffsetInput(null, delegate, offset);
    }
}

public interface RandomInput {

    ByteOrder getByteOrder();

    default boolean isBigEndian() {
        return getByteOrder() == ByteOrder.BIG_ENDIAN;
    }

    void setByteOrder(ByteOrder order);

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

    int size();

    int position();

    void position(int new_position);

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

    RandomInput duplicate();

    default RandomInput duplicate(int offset) {
        RandomInput out = duplicate();
        out.addPosition(offset);
        return out;
    }

    default RandomInput duplicateAt(int offset) {
        RandomInput out = duplicate();
        out.position(offset);
        return out;
    }

    default RandomInput slice() {
        return sliceAt(position());
    }

    default RandomInput slice(int offset) {
        return sliceAt(Math.addExact(position(), offset));
    }

    default RandomInput sliceAt(int offset) {
        return new OffsetInput(this, offset);
    }
}
