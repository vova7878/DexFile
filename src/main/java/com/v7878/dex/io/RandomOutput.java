package com.v7878.dex.io;

import static com.v7878.misc.Math.isAlignedL;
import static com.v7878.misc.Math.roundUpL;

import com.v7878.misc.Checks;

import java.util.Objects;

//TODO: byte order
public interface RandomOutput extends AutoCloseable {

    default void writeByteArray(byte[] arr) {
        writeByteArray(arr, 0, arr.length);
    }

    default void writeByteArray(byte[] arr, int off, int len) {
        Objects.requireNonNull(arr);
        Checks.checkFromIndexSize(off, len, arr.length);
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
        // little-endian
        writeByte(value);
        writeByte(value >> 8);
    }

    default void writeChar(int value) {
        writeShort(value);
    }

    default void writeInt(int value) {
        // little-endian
        writeShort(value);
        writeShort(value >> 16);
    }

    default void writeLong(long value) {
        // little-endian
        writeInt((int) value);
        writeInt((int) (value >> 32));
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

    long size();

    long position();

    long position(long new_position);

    default long addPosition(long delta) {
        return position(position() + delta);
    }

    default long alignPosition(long alignment) {
        return position(roundUpL(position(), alignment));
    }

    default long alignPositionAndFillZeros(long alignment) {
        long old_position = position();
        long new_position = roundUpL(old_position, alignment);
        for (long i = 0; i < new_position - old_position; i++) {
            writeByte(0);
        }
        return old_position;
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

    @Override
    default void close() {
    }
}
