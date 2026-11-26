package com.v7878.dex.util;

/**
 * Shared boxed integers for the pool indexes.
 * <p>
 * Index maps store {@code Map<Id, Integer>}; most dex sections are limited to 16 bits, so the
 * values fit into one small table instead of a fresh {@code Integer} per map entry.
 */
public final class IntegerPool {
    private static final int LIMIT = 0x10000;
    private static final Integer[] POOL = new Integer[LIMIT];

    private IntegerPool() {
    }

    /**
     * The shared instance of {@code value} when it fits into a 16-bit pool index, or a boxed
     * value otherwise.
     */
    public static Integer get(int value) {
        if (value < 0 || value >= LIMIT) {
            return value;
        }
        Integer out = POOL[value];
        if (out == null) {
            POOL[value] = out = value;
        }
        return out;
    }
}
