package com.v7878.dex.smali.parser;

import static com.v7878.dex.util.Checks.shouldNotReachHere;

import java.util.regex.Pattern;

// Note: The string that gets here is already validated
public class LiteralUtils {
    public static char parseChar(String value) {
        value = value.substring(1, value.length() - 1);
        if (value.length() == 1) {
            return value.charAt(0);
        }
        if (value.charAt(0) == '\\') {
            return switch (value.charAt(1)) {
                case 'b' -> '\b';
                case 't' -> '\t';
                case 'n' -> '\n';
                case 'f' -> '\f';
                case 'r' -> '\r';
                case '"' -> '"';
                case '\'' -> '\'';
                case '\\' -> '\\';
                case 'u' -> (char) Integer.parseInt(value.substring(2), 16);
                default -> throw shouldNotReachHere();
            };
        }
        // surrogate pair
        throw new IllegalArgumentException("Invalid char literal: " + value);
    }

    public static String parseString(String value) {
        value = value.substring(1, value.length() - 1);
        var sb = new StringBuilder(value.length());

        for (int i = 0; i < value.length(); ) {
            char c = value.charAt(i++);
            if (c == '\\') {
                switch (value.charAt(i++)) {
                    case 'b' -> sb.append('\b');
                    case 't' -> sb.append('\t');
                    case 'n' -> sb.append('\n');
                    case 'f' -> sb.append('\f');
                    case 'r' -> sb.append('\r');
                    case '"' -> sb.append('"');
                    case '\'' -> sb.append('\'');
                    case '\\' -> sb.append('\\');
                    case 'u' -> sb.append((char) Integer.parseInt(value.substring(i, i += 4), 16));
                    default -> throw shouldNotReachHere();
                }
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    public static int parseInt(String value) {
        return Integer.decode(value);
    }

    public static long parseLong(String value) {
        return Long.decode(value.substring(0, value.length() - 1));
    }

    public static short parseShort(String value) {
        return Short.decode(value.substring(0, value.length() - 1));
    }

    public static byte parseByte(String value) {
        return Byte.decode(value.substring(0, value.length() - 1));
    }

    private static final Pattern SPECIAL_FLOAT_REGEX = Pattern.compile(
            "((-)?infinityf)|(nanf)", Pattern.CASE_INSENSITIVE
    );

    public static float parseFloat(String value) {
        var m = SPECIAL_FLOAT_REGEX.matcher(value);
        if (m.matches()) {
            if (m.start(1) != -1) {
                if (m.start(2) != -1) {
                    return Float.NEGATIVE_INFINITY;
                } else {
                    return Float.POSITIVE_INFINITY;
                }
            } else {
                return Float.NaN;
            }
        }
        return Float.parseFloat(value);
    }

    private static final Pattern SPECIAL_DOUBLE_REGEX = Pattern.compile(
            "((-)?infinityd?)|(nand?)", Pattern.CASE_INSENSITIVE
    );

    public static double parseDouble(String value) {
        var m = SPECIAL_DOUBLE_REGEX.matcher(value);
        if (m.matches()) {
            if (m.start(1) != -1) {
                if (m.start(2) != -1) {
                    return Double.NEGATIVE_INFINITY;
                } else {
                    return Double.POSITIVE_INFINITY;
                }
            } else {
                return Double.NaN;
            }
        }
        return Double.parseDouble(value);
    }

    public static boolean parseBool(String value) {
        return switch (value) {
            case "true" -> true;
            case "false" -> false;
            default -> throw shouldNotReachHere();
        };
    }

    public static String parseSimpleName(String value) {
        // TODO
        return value;
    }

    public static String parseMemberName(String value) {
        // TODO
        return value;
    }
}
