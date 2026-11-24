package com.v7878.dex.io;

import java.util.Arrays;

/**
 * Reads and writes MUTF-8, the dex string encoding: UTF-8 restricted to
 * 1..3-byte forms, U+0000 in two-byte form and a plain zero byte terminator.
 * Encodings do not have to be minimal: any 1..3-byte form decodes.
 */
class MUTF8 {

    public static String readMUTF8(RandomInput in) {
        int pos = in.position();
        int expected_length = in.readULeb128();
        return readMUTF8(in, expected_length, pos);
    }

    public static String readMUTF8(RandomInput in, int expected_length) {
        return readMUTF8(in, expected_length, in.position());
    }

    private static String readMUTF8(RandomInput in, int expected_length, int pos) {
        // Every UTF-16 code unit costs at least one byte, so a declared
        // length above the available bytes can never decode.
        int available = in.available();
        if (expected_length > available) {
            throw new IllegalStateException("MUTF-8 string at position " + pos
                    + ": declared length " + expected_length
                    + " exceeds " + available + " available bytes");
        }
        // Never allocate from the declared length alone: the check above
        // bounds it by the available bytes, and the buffer grows on demand.
        char[] out = new char[Math.max(expected_length, 16)];
        int s = 0;
        while (true) {
            if (in.available() == 0) {
                throw new IllegalStateException(
                        "Truncated MUTF-8 sequence at position " + in.position());
            }
            int a = in.readUByte();
            if (a == 0) {
                break;
            }
            if (a < 0x80) {
                out = append(out, s++, (char) a);
            } else if ((a & 0xe0) == 0xc0) {
                int b = readContinuation(in);
                out = append(out, s++, (char) (((a & 0x1f) << 6) | (b & 0x3f)));
            } else if ((a & 0xf0) == 0xe0) {
                int b = readContinuation(in);
                int c = readContinuation(in);
                out = append(out, s++, (char) (((a & 0x0f) << 12)
                        | ((b & 0x3f) << 6) | (c & 0x3f)));
            } else {
                throw new IllegalStateException("Bad MUTF-8 start byte 0x"
                        + Integer.toHexString(a) + " at position "
                        + (in.position() - 1));
            }
        }
        String result = new String(out, 0, s);
        if (result.length() != expected_length) {
            throw new IllegalStateException("MUTF-8 string at position " + pos
                    + ": declared length " + expected_length
                    + " doesn't match decoded length of " + result.length());
        }
        return result;
    }

    private static int readContinuation(RandomInput in) {
        if (in.available() == 0) {
            throw new IllegalStateException(
                    "Truncated MUTF-8 sequence at position " + in.position());
        }
        int b = in.readUByte();
        if ((b & 0xc0) != 0x80) {
            throw new IllegalStateException("Bad MUTF-8 continuation byte at position "
                    + (in.position() - 1));
        }
        return b;
    }

    private static char[] append(char[] out, int index, char value) {
        if (index == out.length) {
            int grown = out.length * 2;
            if (grown < 0) {
                throw new OutOfMemoryError("MUTF-8 string too large");
            }
            out = Arrays.copyOf(out, grown);
        }
        out[index] = value;
        return out;
    }

    private static void encode(RandomOutput out, String s) {
        final int length = s.length();
        for (int i = 0; i < length; i++) {
            char ch = s.charAt(i);
            if (ch != 0 && ch <= 127) { // U+0000 uses two bytes
                out.writeByte(ch);
            } else if (ch <= 2047) {
                out.writeByte(0xc0 | (0x1f & (ch >> 6)));
                out.writeByte(0x80 | (0x3f & ch));
            } else {
                out.writeByte(0xe0 | (0x0f & (ch >> 12)));
                out.writeByte(0x80 | (0x3f & (ch >> 6)));
                out.writeByte(0x80 | (0x3f & ch));
            }
        }
    }

    public static void writeMUTF8(RandomOutput out, String s) {
        out.writeULeb128(s.length());
        encode(out, s);
        out.writeByte(0);
    }
}
