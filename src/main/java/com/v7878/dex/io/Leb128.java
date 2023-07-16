package com.v7878.dex.io;

public class Leb128 {

    private Leb128() {
    }

    public static int readSignedLeb128(RandomInput in) {
        int result = 0;
        int cur;
        int count = 0;
        int signBits = -1;
        do {
            cur = in.readByte() & 0xff;
            result |= (cur & 0x7f) << (count * 7);
            signBits <<= 7;
            count++;
        } while (((cur & 0x80) == 0x80) && count < 5);
        if ((cur & 0x80) == 0x80) {
            throw new IllegalStateException("invalid LEB128 sequence");
        }
        // Sign extend if appropriate
        if (((signBits >> 1) & result) != 0) {
            result |= signBits;
        }
        return result;
    }

    public static int readUnsignedLeb128(RandomInput in) {
        int result = 0;
        int cur;
        int count = 0;
        do {
            cur = in.readByte() & 0xff;
            result |= (cur & 0x7f) << (count * 7);
            count++;
        } while (((cur & 0x80) == 0x80) && count < 5);
        if ((cur & 0x80) == 0x80) {
            throw new IllegalStateException("invalid LEB128 sequence");
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
        out.writeByte((byte) (value & 0x7f));
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
