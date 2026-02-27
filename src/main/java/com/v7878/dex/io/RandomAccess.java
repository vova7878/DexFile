package com.v7878.dex.io;

import static com.v7878.dex.util.MathUtils.isAligned;
import static com.v7878.dex.util.MathUtils.isPowerOfTwo;
import static com.v7878.dex.util.MathUtils.roundUp;

import java.nio.ByteOrder;

public interface RandomAccess {
    ByteOrder getByteOrder();

    default boolean isBigEndian() {
        return getByteOrder() == ByteOrder.BIG_ENDIAN;
    }

    void setByteOrder(ByteOrder order);

    int size();

    int position();

    void position(int position);

    default void addPosition(int delta) {
        position(Math.addExact(position(), delta));
    }

    default void alignPosition(int alignment) {
        assert isPowerOfTwo(alignment);
        position(roundUp(position(), alignment));
    }

    default void requireAlignment(int alignment) {
        assert isPowerOfTwo(alignment);
        int pos = position();
        if (!isAligned(pos, alignment)) {
            throw new IllegalStateException("Position " + pos + " not aligned by " + alignment);
        }
    }

    default RandomAccess duplicate() {
        return duplicateAt(position());
    }

    default RandomAccess duplicate(int offset) {
        return duplicateAt(Math.addExact(position(), offset));
    }

    RandomAccess duplicateAt(int position);

    RandomAccess markAsStart();
}
