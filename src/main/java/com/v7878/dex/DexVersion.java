package com.v7878.dex;

import java.util.Arrays;

public enum DexVersion implements Comparable<DexVersion> {
    //TODO: DEX041(35, '0', '4', '1'); // dex containers,
    DEX040(29, 'd', 'e', 'x', '\n', '0', '4', '0', '\0'),
    DEX039(28, 'd', 'e', 'x', '\n', '0', '3', '9', '\0'),
    DEX038(26, 'd', 'e', 'x', '\n', '0', '3', '8', '\0'),
    DEX037(24, 'd', 'e', 'x', '\n', '0', '3', '7', '\0'),
    DEX035(1, 'd', 'e', 'x', '\n', '0', '3', '5', '\0'),
    CDEX001(28, 'c', 'd', 'e', 'x', '0', '0', '1', '\0');

    private final int minApi;
    private final long value;

    private static long getValue(long b0, long b1, long b2, long b3, long b4, long b5, long b6, long b7) {
        return b0 << 56 | b1 << 48 | b2 << 40 | b3 << 32 | b4 << 24 | b5 << 16 | b6 << 8 | b7;
    }

    private static byte[] getArray(long value) {
        return new byte[]{
                (byte) (value >>> 56),
                (byte) (value >> 48 & 0xffL),
                (byte) (value >> 40 & 0xffL),
                (byte) (value >> 32 & 0xffL),
                (byte) (value >> 24 & 0xffL),
                (byte) (value >> 16 & 0xffL),
                (byte) (value >> 8 & 0xffL),
                (byte) (value & 0xffL)
        };
    }

    DexVersion(int minApi, long b0, long b1, long b2, long b3, long b4, long b5, long b6, long b7) {
        this.minApi = minApi;
        this.value = getValue(b0, b1, b2, b3, b4, b5, b6, b7);
    }

    public boolean isCompact() {
        return this == CDEX001;
    }

    public int getMinApi() {
        return minApi;
    }

    public static DexVersion forApi(int api) {
        for (DexVersion version : values()) {
            if (api >= version.minApi) {
                return version;
            }
        }
        throw new IllegalArgumentException("Can`t find DexVersion for api " + api);
    }

    public byte[] getMagic() {
        return getArray(value);
    }

    public static DexVersion forMagic(byte[] magic) {
        if (magic.length != 8) {
            throw new IllegalArgumentException("invalid magic length: " + magic.length);
        }

        long value = getValue(
                magic[0] & 0xffL, magic[1] & 0xffL,
                magic[2] & 0xffL, magic[3] & 0xffL,
                magic[4] & 0xffL, magic[5] & 0xffL,
                magic[6] & 0xffL, magic[7] & 0xffL);

        for (DexVersion version : values()) {
            if (value == version.value) {
                return version;
            }
        }

        throw new IllegalArgumentException("Unknown dex version: " + Arrays.toString(magic));
    }
}
