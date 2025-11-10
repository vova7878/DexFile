package com.v7878.dex;

import static com.v7878.dex.DexConstants.VISIBILITY_BUILD;
import static com.v7878.dex.DexConstants.VISIBILITY_RUNTIME;
import static com.v7878.dex.DexConstants.VISIBILITY_SYSTEM;

public enum AnnotationVisibility {
    BUILD(VISIBILITY_BUILD),
    RUNTIME(VISIBILITY_RUNTIME),
    SYSTEM(VISIBILITY_SYSTEM);

    private final int value;

    AnnotationVisibility(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    public static AnnotationVisibility of(int value) {
        return switch (value) {
            case VISIBILITY_BUILD -> BUILD;
            case VISIBILITY_RUNTIME -> RUNTIME;
            case VISIBILITY_SYSTEM -> SYSTEM;
            default -> throw new IllegalArgumentException(
                    "Unknown annotation visibility: " + value);
        };
    }
}
