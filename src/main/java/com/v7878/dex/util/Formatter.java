package com.v7878.dex.util;

public class Formatter {
    public static String signedHex(int i) {
        boolean neg = i < 0;
        i = Math.abs(i);
        return (neg ? "-" : "") + "0x" + Integer.toHexString(i);
    }

    public static String unsignedHex(int i) {
        return "0x" + Integer.toHexString(i);
    }

    public static String signedHex(long i) {
        boolean neg = i < 0;
        i = Math.abs(i);
        return (neg ? "-" : "") + "0x" + Long.toHexString(i);
    }

    public static String unsignedHex(long i) {
        return "0x" + Long.toHexString(i);
    }

    public static String register(int i) {
        return "v" + i;
    }
}
