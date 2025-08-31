package com.v7878.dex.util;

public class Checks {
    public static void checkIndex(int index, int size, String name) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(
                    String.format("Invalid %s index %d, not in [0, %d)", name, index, size));
        }
    }

    public static AssertionError shouldNotReachHere() {
        throw new AssertionError("Should not reach here");
    }

    public static AssertionError shouldNotHappen(Throwable th) {
        throw new AssertionError("Should not happen", th);
    }
}
