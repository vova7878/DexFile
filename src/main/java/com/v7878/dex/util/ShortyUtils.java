package com.v7878.dex.util;

import com.v7878.dex.immutable.Parameter;
import com.v7878.dex.immutable.TypeId;

import java.util.List;

public class ShortyUtils {
    public static RuntimeException unrecognizedType(String descriptor) {
        throw new IllegalArgumentException("Unrecognized type: " + descriptor);
    }

    public static RuntimeException unrecognizedShorty(char shorty) {
        throw new IllegalArgumentException("Unrecognized shorty: " + shorty);
    }

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
                throw unrecognizedType(descriptor);
            }
        };
    }

    public static char getTypeShorty(TypeId type) {
        return getTypeShorty(type.getDescriptor());
    }

    public static int getRegisterCountWithCheck(char shorty) {
        return switch (shorty) {
            case 'V' -> 0;
            case 'Z', 'B', 'C', 'S', 'I', 'F', 'L' -> 1;
            case 'J', 'D' -> 2;
            default -> throw unrecognizedShorty(shorty);
        };
    }

    public static int getRegisterCount(char shorty) {
        return shorty == 'V' ? 0 : (shorty == 'D' || shorty == 'J' ? 2 : 1);
    }

    public static int getRegisterCount(String descriptor) {
        return getRegisterCount(getTypeShorty(descriptor));
    }

    public static int getRegisterCount(TypeId type) {
        return getRegisterCount(type.getDescriptor());
    }

    public static String getShorty(TypeId return_type, List<TypeId> parameters) {
        var out = new StringBuilder(parameters.size() + 1);
        out.append(return_type.getShorty());
        for (var tmp : parameters) {
            out.append(tmp.getShorty());
        }
        return out.toString();
    }

    public static String getDefShorty(TypeId return_type, List<Parameter> parameters) {
        var out = new StringBuilder(parameters.size() + 1);
        out.append(return_type.getShorty());
        for (var tmp : parameters) {
            out.append(tmp.getType().getShorty());
        }
        return out.toString();
    }

    public static int getInputRegisterCount(List<TypeId> parameters) {
        int out = 0;
        for (var tmp : parameters) {
            out += tmp.getRegisterCount();
        }
        return out;
    }

    public static int getDefInputRegisterCount(List<Parameter> parameters) {
        int out = 0;
        for (var tmp : parameters) {
            out += tmp.getType().getRegisterCount();
        }
        return out;
    }
}
