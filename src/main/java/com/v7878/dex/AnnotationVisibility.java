package com.v7878.dex;

import static com.v7878.dex.DexConstants.VISIBILITY_BUILD;
import static com.v7878.dex.DexConstants.VISIBILITY_RUNTIME;
import static com.v7878.dex.DexConstants.VISIBILITY_SYSTEM;

public enum AnnotationVisibility {
    BUILD(VISIBILITY_BUILD, "build"),
    RUNTIME(VISIBILITY_RUNTIME, "runtime"),
    SYSTEM(VISIBILITY_SYSTEM, "system");

    private final int value;
    private final String name;

    AnnotationVisibility(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public int value() {
        return value;
    }

    public static AnnotationVisibility of(String name) {
        return switch (name) {
            case "build" -> BUILD;
            case "runtime" -> RUNTIME;
            case "system" -> SYSTEM;
            default -> throw new IllegalArgumentException(
                    "Unknown annotation visibility: " + name);
        };
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

    @Override
    public String toString() {
        return name;
    }
}
