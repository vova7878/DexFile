package com.v7878.dex.io;

import static com.v7878.dex.util.AlignmentUtils.isAligned;
import static com.v7878.dex.util.AlignmentUtils.roundUp;

import java.nio.ByteOrder;
import java.util.Objects;

class OffsetOutput implements RandomOutput {

    private final RandomOutput delegate;
    private final int offset;

    OffsetOutput(Void ignored, RandomOutput delegate, int offset) {
        this.delegate = delegate.duplicate();
        this.offset = offset;
    }

    OffsetOutput(RandomOutput delegate, int offset) {
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
    public RandomOutput duplicate() {
        return new OffsetOutput(null, delegate, offset);
    }
}

public interface RandomOutput {

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

    default void writeLongArray(long[] ints) {
        for (long value : ints) {
            writeLong(value);
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

    int size();

    int position();

    void position(int new_position);

    default void addPosition(int delta) {
        position(Math.addExact(position(), delta));
    }

    default void alignPosition(int alignment) {
        position(roundUp(position(), alignment));
    }

    default void fillZerosToAlignment(int alignment) {
        int old_position = position();
        int new_position = roundUp(old_position, alignment);
        for (long i = 0; i < new_position - old_position; i++) {
            writeByte(0);
        }
    }

    default void requireAlignment(int alignment) {
        int pos = position();
        if (!isAligned(pos, alignment)) {
            throw new IllegalStateException("Position " + pos + " not aligned by " + alignment);
        }
    }

    RandomOutput duplicate();

    default RandomOutput duplicate(int offset) {
        RandomOutput out = duplicate();
        out.addPosition(offset);
        return out;
    }

    default RandomOutput duplicateAt(int offset) {
        RandomOutput out = duplicate();
        out.position(offset);
        return out;
    }

    default RandomOutput slice() {
        return sliceAt(position());
    }

    default RandomOutput slice(int offset) {
        return sliceAt(Math.addExact(position(), offset));
    }

    default RandomOutput sliceAt(int offset) {
        return new OffsetOutput(this, offset);
    }
}
