package com.v7878.dex.util;

public class Exceptions {
    public static AssertionError shouldNotReachHere() {
        throw new AssertionError("Should not reach here");
    }

    public static AssertionError shouldNotHappen(Throwable th) {
        throw new AssertionError("Should not happen", th);
    }
}
