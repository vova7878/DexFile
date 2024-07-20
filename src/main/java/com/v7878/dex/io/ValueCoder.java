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

import com.v7878.dex.EncodedValue.EncodedValueType;

public final class ValueCoder {

    private ValueCoder() {
    }

    public static void writeSignedIntegralValue(
            RandomOutput out, EncodedValueType type, long value) {
        /*
         * Figure out how many bits are needed to represent the value,
         * including a sign bit: The bit count is subtracted from 65
         * and not 64 to account for the sign bit. The xor operation
         * has the effect of leaving non-negative values alone and
         * unary complementing negative values (so that a leading zero
         * count always returns a useful number for our present
         * purpose).
         */
        int requiredBits = 65 - Long.numberOfLeadingZeros(value ^ (value >> 63));

        // Round up the requiredBits to a number of bytes.
        int requiredBytes = (requiredBits + 0x07) >> 3;

        /*
         * Write the header byte, which includes the type and
         * requiredBytes - 1.
         */
        out.writeByte(type.value | ((requiredBytes - 1) << 5));

        // Write the value, per se.
        while (requiredBytes > 0) {
            out.writeByte((byte) value);
            value >>= 8;
            requiredBytes--;
        }
    }

    public static void writeUnsignedIntegralValue(
            RandomOutput out, EncodedValueType type, long value) {
        // Figure out how many bits are needed to represent the value.
        int requiredBits = 64 - Long.numberOfLeadingZeros(value);
        if (requiredBits == 0) {
            requiredBits = 1;
        }

        // Round up the requiredBits to a number of bytes.
        int requiredBytes = (requiredBits + 0x07) >> 3;

        /*
         * Write the header byte, which includes the type and
         * requiredBytes - 1.
         */
        out.writeByte(type.value | ((requiredBytes - 1) << 5));

        // Write the value, per se.
        while (requiredBytes > 0) {
            out.writeByte((byte) value);
            value >>= 8;
            requiredBytes--;
        }
    }

    public static void writeRightZeroExtendedValue(
            RandomOutput out, EncodedValueType type, long value) {
        // Figure out how many bits are needed to represent the value.
        int requiredBits = 64 - Long.numberOfTrailingZeros(value);
        if (requiredBits == 0) {
            requiredBits = 1;
        }

        // Round up the requiredBits to a number of bytes.
        int requiredBytes = (requiredBits + 0x07) >> 3;

        // Scootch the first bits to be written down to the low-order bits.
        value >>= 64 - (requiredBytes * 8);

        /*
         * Write the header byte, which includes the type and
         * requiredBytes - 1.
         */
        out.writeByte(type.value | ((requiredBytes - 1) << 5));

        // Write the value, per se.
        while (requiredBytes > 0) {
            out.writeByte((byte) value);
            value >>= 8;
            requiredBytes--;
        }
    }

    public static int readSignedInt(RandomInput in, int zwidth) {
        int result = 0;
        for (int i = zwidth; i >= 0; i--) {
            result = (result >>> 8) | (in.readUnsignedByte() << 24);
        }
        result >>= (3 - zwidth) * 8;
        return result;
    }

    public static int readUnsignedInt(RandomInput in, int zwidth, boolean fillOnRight) {
        int result = 0;
        for (int i = zwidth; i >= 0; i--) {
            result = (result >>> 8) | (in.readUnsignedByte() << 24);
        }
        if (!fillOnRight) {
            result >>>= (3 - zwidth) * 8;
        }
        return result;
    }

    public static long readSignedLong(RandomInput in, int zwidth) {
        long result = 0;
        for (int i = zwidth; i >= 0; i--) {
            result = (result >>> 8) | ((in.readByte() & 0xffL) << 56);
        }
        result >>= (7 - zwidth) * 8;
        return result;
    }

    public static long readUnsignedLong(RandomInput in, int zwidth, boolean fillOnRight) {
        long result = 0;
        for (int i = zwidth; i >= 0; i--) {
            result = (result >>> 8) | ((in.readByte() & 0xffL) << 56);
        }
        if (!fillOnRight) {
            result >>>= (7 - zwidth) * 8;
        }
        return result;
    }
}
