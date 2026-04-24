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

    private static long parseSULong(String nm) {
        int radix = 10;
        int index = 0;
        boolean negative = false;

        if (nm.isEmpty()) {
            throw new NumberFormatException("Zero length string");
        }

        char firstChar = nm.charAt(0);
        // Handle sign, if present
        if (firstChar == '-') {
            negative = true;
            index++;
        } else if (firstChar == '+') {
            index++;
        }

        // Handle radix specifier, if present
        if (nm.startsWith("0x", index) || nm.startsWith("0X", index)) {
            index += 2;
            radix = 16;
        } else if (nm.startsWith("0", index) && nm.length() > 1 + index) {
            index++;
            radix = 8;
        }

        if (nm.startsWith("-", index) || nm.startsWith("+", index))
            throw new NumberFormatException("Sign character in wrong position");

        long result = Long.parseUnsignedLong(nm.substring(index), radix);
        if (negative && Long.compareUnsigned(Long.MIN_VALUE, result) < 0)
            throw new NumberFormatException(String.format(
                    "String value %s exceeds range of long", nm));
        return negative ? -result : result;
    }

    public static int parseInt(String value) {
        long l = parseSULong(value);
        if (l < Integer.MIN_VALUE || l > 0xffffffffL)
            throw new NumberFormatException(String.format(
                    "String value %s exceeds range of 4-byte integer", value));
        return (int) l;
    }

    public static long parseLong(String value) {
        return parseSULong(value.substring(0, value.length() - 1));
    }

    public static short parseShort(String value) {
        value = value.substring(0, value.length() - 1);
        long l = parseSULong(value);
        if (l < Short.MIN_VALUE || l > 0xffffL)
            throw new NumberFormatException(String.format(
                    "String value %s exceeds range of 2-byte integer", value));
        return (short) l;
    }

    public static byte parseByte(String value) {
        value = value.substring(0, value.length() - 1);
        long l = parseSULong(value);
        if (l < Byte.MIN_VALUE || l > 0xffL)
            throw new NumberFormatException(String.format(
                    "String value %s exceeds range of 1-byte integer", value));
        return (byte) l;
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
        // TODO: validate
        return value.replace("\\ ", " ");
    }

    public static String parseMemberName(String value) {
        // TODO: validate
        return parseSimpleName(value);
    }
}
