package com.v7878.dex.io;

import static com.v7878.misc.Math.isAlignedL;
import static com.v7878.misc.Math.roundUpL;

public interface RandomIO extends RandomInput, RandomOutput {

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

    @Override
    RandomIO duplicate();

    @Override
    default RandomIO duplicate(long offset) {
        RandomIO out = duplicate();
        out.addPosition(offset);
        return out;
    }

    @Override
    default void close() {
    }
}
