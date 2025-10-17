package com.v7878.dex.io;

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
    RandomIO markAsStart();
}
