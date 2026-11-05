package com.v7878.dex.io;

/**
 * Reads and writes LEB128, the variable-length 7-bits-per-byte integer
 * encoding used for most dex counts and offsets.
 */
class Leb128 {

    /**
     * Strict mode (default): the fifth byte must not carry value bits above
     * bit 31, so every decoded value fits in an int.
     * ART-tolerant mode: excess fifth-byte bits are silently truncated,
     * matching the ART 32-bit decoder.
     */
    static final boolean ART_TOLERANT_EXCESS_BITS = false;

    public static int readSignedLeb128(RandomInput in) {
        return readSignedLeb128(in, ART_TOLERANT_EXCESS_BITS);
    }

    static int readSignedLeb128(RandomInput in, boolean tolerant) {
        int pos = in.position();
        int result = 0;
        int cur;
        int count = 0;
        int signBits = -1;
        do {
            cur = in.readUByte();
            result |= (cur & 0x7f) << (count * 7);
            signBits <<= 7;
            count++;
        } while (((cur & 0x80) == 0x80) && count < 5);
        if ((cur & 0x80) == 0x80) {
            throw new IllegalStateException(
                    "Invalid LEB128 sequence at position " + pos);
        }
        // The fifth byte holds value bits 28..34: bits 32..34 must repeat
        // bit 31, else the encoded value does not fit in an int
        if (count == 5 && !tolerant
                && (cur & 0x70) != ((result & 0x80000000) != 0 ? 0x70 : 0)) {
            throw new IllegalStateException(
                    "Invalid sleb128 sequence at position " + pos);
        }
        // Sign extend if appropriate
        if (((signBits >> 1) & result) != 0) {
            result |= signBits;
        }
        return result;
    }

    public static int readUnsignedLeb128(RandomInput in) {
        return readUnsignedLeb128(in, ART_TOLERANT_EXCESS_BITS);
    }

    static int readUnsignedLeb128(RandomInput in, boolean tolerant) {
        int pos = in.position();
        int result = 0;
        int cur;
        int count = 0;
        do {
            cur = in.readUByte();
            result |= (cur & 0x7f) << (count * 7);
            count++;
        } while (((cur & 0x80) == 0x80) && count < 5);
        if ((cur & 0x80) == 0x80) {
            throw new IllegalStateException(
                    "Invalid LEB128 sequence at position " + pos);
        }
        // The fifth byte holds value bits 28..34: bits 32..34 must be zero,
        // else the encoded value does not fit in an int
        if (count == 5 && !tolerant && (cur & 0x70) != 0) {
            throw new IllegalStateException(
                    "Invalid uleb128 sequence at position " + pos);
        }
        return result;
    }

    public static void writeUnsignedLeb128(RandomOutput out, int value) {
        int remaining = value >>> 7;
        while (remaining != 0) {
            out.writeByte((value & 0x7f) | 0x80);
            value = remaining;
            remaining >>>= 7;
        }
        out.writeByte(value & 0x7f);
    }

    public static void writeSignedLeb128(RandomOutput out, int value) {
        int remaining = value >> 7;
        boolean hasMore = true;
        int end = ((value & Integer.MIN_VALUE) == 0) ? 0 : -1;
        while (hasMore) {
            hasMore = (remaining != end)
                    || ((remaining & 1) != ((value >> 6) & 1));
            out.writeByte((value & 0x7f) | (hasMore ? 0x80 : 0));
            value = remaining;
            remaining >>= 7;
        }
    }
}
