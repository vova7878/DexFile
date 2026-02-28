package com.v7878.dex.io;

import static com.v7878.dex.util.MathUtils.isPowerOfTwo;
import static com.v7878.dex.util.MathUtils.roundUp;

import java.util.Objects;

public interface RandomOutput extends RandomAccess {
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

    default void readFrom(RandomInput in, int length) {
        for (int i = 0; i < length; i++) {
            writeByte(in.readByte());
        }
    }

    default void readFrom(RandomInput in) {
        readFrom(in, size() - position());
    }

    default void fillZerosToAlignment(int alignment) {
        assert isPowerOfTwo(alignment);
        int old_position = position();
        int new_position = roundUp(old_position, alignment);
        for (long i = 0; i < new_position - old_position; i++) {
            writeByte(0);
        }
    }

    @Override
    default RandomOutput duplicate() {
        return duplicateAt(position());
    }

    @Override
    default RandomOutput duplicate(int offset) {
        return duplicateAt(Math.addExact(position(), offset));
    }

    @Override
    RandomOutput duplicateAt(int position);

    @Override
    RandomOutput markAsStart();
}
