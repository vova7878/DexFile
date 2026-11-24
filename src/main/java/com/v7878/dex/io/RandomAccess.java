package com.v7878.dex.io;

import static com.v7878.dex.util.MathUtils.isAligned;
import static com.v7878.dex.util.MathUtils.isPowerOfTwo;
import static com.v7878.dex.util.MathUtils.roundUp;

import java.nio.ByteOrder;

public interface RandomAccess {

    /**
     * Returns the byte order used by the multi-byte read and write methods.
     */
    ByteOrder getByteOrder();

    default boolean isBigEndian() {
        return getByteOrder() == ByteOrder.BIG_ENDIAN;
    }

    /**
     * Sets the byte order used by the multi-byte read and write methods.
     */
    void setByteOrder(ByteOrder order);

    /**
     * Returns the number of bytes between the marked start and the end of
     * the underlying data.
     */
    int size();

    /**
     * Returns the current position relative to the marked start; it can be
     * negative after {@link #markAsStart()}.
     */
    int position();

    /**
     * Sets the position relative to the marked start.
     */
    void position(int position);

    /**
     * Returns the number of bytes still readable from the current position;
     * never negative. After {@link #markAsStart()} the position can be
     * negative and the bytes before the new start are counted too, so this
     * can exceed {@link #size()}.
     */
    default int available() {
        return size() - position();
    }

    /**
     * Moves the position by delta, failing on int overflow.
     */
    default void addPosition(int delta) {
        position(Math.addExact(position(), delta));
    }

    /**
     * Moves the position up to the next multiple of alignment.
     */
    default void alignPosition(int alignment) {
        assert isPowerOfTwo(alignment);
        position(roundUp(position(), alignment));
    }

    /**
     * Fails if the position is not a multiple of alignment.
     */
    default void requireAlignment(int alignment) {
        assert isPowerOfTwo(alignment);
        int pos = position();
        if (!isAligned(pos, alignment)) {
            throw new IllegalStateException("Position " + pos + " not aligned by " + alignment);
        }
    }

    /**
     * Returns a copy sharing the underlying data, positioned at the current
     * position.
     */
    default RandomAccess duplicate() {
        return duplicateAt(position());
    }

    /**
     * Returns a copy sharing the underlying data, positioned offset bytes
     * from the current position.
     */
    default RandomAccess duplicate(int offset) {
        return duplicateAt(Math.addExact(position(), offset));
    }

    /**
     * Returns a copy sharing the underlying data at the given position.
     */
    RandomAccess duplicateAt(int position);

    /**
     * Moves the zero point to the current position: later positions are
     * measured from it and can be negative.
     */
    RandomAccess markAsStart();
}
