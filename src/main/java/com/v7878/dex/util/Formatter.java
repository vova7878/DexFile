package com.v7878.dex.util;

import static com.v7878.dex.analysis.Position.EXCEPTION_REGISTER;
import static com.v7878.dex.analysis.Position.RESULT_REGISTER;

import com.v7878.collections.IntSet;

import java.util.stream.Collectors;

public class Formatter {
    public static String signedHex(int i) {
        boolean neg = i < 0;
        i = Math.abs(i);
        return (neg ? "-" : "+") + "0x" + Integer.toHexString(i);
    }

    public static String unsignedHex(int i) {
        return "0x" + Integer.toHexString(i);
    }

    public static String signedHex(long i) {
        boolean neg = i < 0;
        i = Math.abs(i);
        return (neg ? "-" : "+") + "0x" + Long.toHexString(i);
    }

    public static String unsignedHex(long i) {
        return "0x" + Long.toHexString(i);
    }

    public static String register(int i) {
        if (i == EXCEPTION_REGISTER) {
            return "exception";
        }
        if (i == RESULT_REGISTER) {
            return "result[0]";
        }
        if (i == RESULT_REGISTER + 1) {
            return "result[1]";
        }
        return "v" + i;
    }

    public static String registers(IntSet regs) {
        return regs.stream().mapToObj(Formatter::register)
                .collect(Collectors.joining(", ", "{", "}"));
    }
}
