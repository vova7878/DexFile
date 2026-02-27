package com.v7878.dex.util;

public class MathUtils {
    public static boolean isPowerOfTwo(int value) {
        return (value & (value - 1)) == 0;
    }

    public static boolean isAligned(int value, int alignment) {
        assert isPowerOfTwo(alignment);
        return (value & (alignment - 1)) == 0;
    }

    public static int roundDown(int x, int alignment) {
        assert isPowerOfTwo(alignment);
        return x & -alignment;
    }

    public static int roundUp(int x, int alignment) {
        assert isPowerOfTwo(alignment);
        return roundDown(Math.addExact(x, alignment - 1), alignment);
    }

    public static boolean swidth(int value, int width) {
        int empty_width = 32 - width;
        return value << empty_width >> empty_width == value;
    }

    public static boolean uwidth(int value, int width) {
        return value >>> width == 0;
    }

    public static boolean hwidth32(int value, int width) {
        return (value & (-1 >>> width)) == 0;
    }

    public static boolean swidth(long value, int width) {
        int empty_width = 64 - width;
        return value << empty_width >> empty_width == value;
    }

    public static boolean uwidth(long value, int width) {
        return value >>> width == 0L;
    }

    public static boolean hwidth64(long value, int width) {
        return (value & (-1L >>> width)) == 0;
    }
}
