package com.v7878.dex.util;

public class Checks {
    public static long checkPosition(long position, long length) {
        if (position < 0 || position > length) {
            throw new IndexOutOfBoundsException(
                    String.format("Position %s out of bounds for length %s",
                            position, length));
        }
        return position;
    }

    public static int checkPosition(int position, int length) {
        if (position < 0 || position > length) {
            throw new IndexOutOfBoundsException(
                    String.format("Position %s out of bounds for length %s",
                            position, length));
        }
        return position;
    }

    public static int checkRange(int value, int start, int length) {
        if (length < 0 || value < start || value >= start + length) {
            throw new IndexOutOfBoundsException(
                    String.format("value %s out of range [%s, %<s + %s)",
                            value, start, length));
        }
        return value;
    }

    public static long checkRange(long value, long start, long length) {
        if (length < 0 || value < start || value >= start + length) {
            throw new IndexOutOfBoundsException(
                    String.format("value %s out of range [%s, %<s + %s)",
                            value, start, length));
        }
        return value;
    }
}
