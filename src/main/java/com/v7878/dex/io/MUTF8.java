/*
 * Copyright (c) 2023 Vladimir Kozelkov
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.v7878.dex.io;

public class MUTF8 {

    private MUTF8() {
    }

    public static String decode(RandomInput in, char[] out) {
        int s = 0;
        while (true) {
            char a = (char) (in.readByte() & 0xff);
            if (a == 0) {
                return new String(out, 0, s);
            }
            out[s] = a;
            if (a < '\u0080') {
                s++;
            } else if ((a & 0xe0) == 0xc0) {
                int b = in.readByte() & 0xff;
                if ((b & 0xC0) != 0x80) {
                    throw new IllegalStateException("bad second byte");
                }
                out[s++] = (char) (((a & 0x1F) << 6) | (b & 0x3F));
            } else if ((a & 0xf0) == 0xe0) {
                int b = in.readByte() & 0xff;
                int c = in.readByte() & 0xff;
                if (((b & 0xC0) != 0x80) || ((c & 0xC0) != 0x80)) {
                    throw new IllegalStateException("bad second or third byte");
                }
                out[s++] = (char) (((a & 0x0F) << 12) | ((b & 0x3F) << 6) | (c & 0x3F));
            } else {
                throw new IllegalStateException("bad byte");
            }
        }
    }

    public static String readMUTF8(RandomInput in) {
        int expectedLength = in.readULeb128();
        String result = decode(in, new char[expectedLength]);
        if (result.length() != expectedLength) {
            throw new IllegalStateException("Declared length " + expectedLength
                    + " doesn't match decoded length of " + result.length());
        }
        return result;
    }

    //TODO: delete?
    private static int countBytes(String s, boolean shortLength) {
        int result = 0;
        final int length = s.length();
        for (int i = 0; i < length; ++i) {
            char ch = s.charAt(i);
            if (ch != 0 && ch <= 127) { // U+0000 uses two bytes.
                ++result;
            } else if (ch <= 2047) {
                result += 2;
            } else {
                result += 3;
            }
            if (shortLength && result > 65535) {
                throw new IllegalArgumentException("String more than 65535 UTF bytes long");
            }
        }
        return result;
    }

    public static void encode(RandomOutput out, String s) {
        final int length = s.length();
        for (int i = 0; i < length; i++) {
            char ch = s.charAt(i);
            if (ch != 0 && ch <= 127) { // U+0000 uses two bytes.
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
