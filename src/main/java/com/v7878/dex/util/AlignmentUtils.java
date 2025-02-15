package com.v7878.dex.util;

public class AlignmentUtils {
    public static boolean isAligned(int value, int alignment) {
        return (value & (alignment - 1)) == 0;
    }

    public static int roundDown(int x, int alignment) {
        return x & -alignment;
    }

    public static int roundUp(int x, int alignment) {
        return roundDown(Math.addExact(x, alignment - 1), alignment);
    }
}
