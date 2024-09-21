package com.v7878.dex.util;

import com.v7878.dex.immutable.TypeId;

public class ShortyUtils {
    public static char getTypeShorty(String descriptor) {
        return switch (descriptor) {
            case "V" -> 'V';
            case "Z" -> 'Z';
            case "B" -> 'B';
            case "S" -> 'S';
            case "C" -> 'C';
            case "I" -> 'I';
            case "J" -> 'J';
            case "F" -> 'F';
            case "D" -> 'D';
            default -> {
                if (descriptor.startsWith("L") || descriptor.startsWith("[")) {
                    yield 'L';
                }
                throw new IllegalArgumentException("Unrecognized type: " + descriptor);
            }
        };
    }

    public static char getTypeShorty(TypeId type) {
        return getTypeShorty(type.getDescriptor());
    }

    public static int getRegisterCount(String descriptor) {
        char shorty = getTypeShorty(descriptor);
        return shorty == 'V' ? 0 : (shorty == 'D' || shorty == 'J' ? 2 : 1);
    }

    public static int getRegisterCount(TypeId type) {
        return getRegisterCount(type.getDescriptor());
    }
}
