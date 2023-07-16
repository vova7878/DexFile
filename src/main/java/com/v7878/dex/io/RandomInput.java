package com.v7878.dex.io;

import static com.v7878.misc.Math.isAlignedL;
import static com.v7878.misc.Math.roundUpL;

import com.v7878.misc.Checks;

import java.util.Objects;

//TODO: byte order
public interface RandomInput extends AutoCloseable {

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
        // little-endian
        return readUnsignedByte() | (readUnsignedByte() << 8);
    }

    default char readChar() {
        return (char) readUnsignedShort();
    }

    default int readInt() {
        // little-endian
        return readUnsignedShort() | (readUnsignedShort() << 16);
    }

    default long readLong() {
        // little-endian
        return (readInt() & 0xffffffffL) | ((readInt() & 0xffffffffL) << 32);
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
        return position(position() + delta);
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

    @Override
    default void close() {
    }
}
