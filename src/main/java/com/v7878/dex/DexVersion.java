package com.v7878.dex;

import static com.v7878.dex.DexConstants.API_M3;
import static com.v7878.dex.DexConstants.API_M5;

public enum DexVersion {
    // Officially, support is declared starting from API 36,
    // but API 35 actually supports this dex version
    DEX041(35, Integer.MAX_VALUE, 'd', 'e', 'x', '\n', '0', '4', '1', '\0'),
    DEX040(30, Integer.MAX_VALUE, 'd', 'e', 'x', '\n', '0', '4', '0', '\0'),
    DEX039(28, Integer.MAX_VALUE, 'd', 'e', 'x', '\n', '0', '3', '9', '\0'),
    DEX038(26, Integer.MAX_VALUE, 'd', 'e', 'x', '\n', '0', '3', '8', '\0'),
    DEX037(24, Integer.MAX_VALUE, 'd', 'e', 'x', '\n', '0', '3', '7', '\0'),
    // dex036 was never completed. It included new set of 'jumbo' instructions,
    // but the rest of the format remained unchanged, making them unusable.
    // These instructions existed in API 14 and 15, but in API 16
    // all the differences between dex035 and dex036 disappeared.
    // In API 21, the dex036 format was declared 'non-existent'
    DEX036(14, 20, 'd', 'e', 'x', '\n', '0', '3', '6', '\0'),
    DEX035(1, Integer.MAX_VALUE, 'd', 'e', 'x', '\n', '0', '3', '5', '\0'),
    // These versions of dex were used before android 1.0 in
    // milestone 3 and 5 and are the only known early versions of the dex format
    DEX013(API_M5, API_M5, 'd', 'e', 'x', '\n', '0', '1', '3', '\0'),
    DEX009(API_M3, API_M3, 'd', 'e', 'x', '\n', '0', '0', '9', '\0'),
    // cdex is an internal file format that existed between API 28 and 35 inclusive
    CDEX001(28, 35, 'c', 'd', 'e', 'x', '0', '0', '1', '\0');

    private final int minApi;
    private final int maxApi;
    private final long magic;

    DexVersion(int minApi, int maxApi, long b0, long b1, long b2, long b3, long b4, long b5, long b6, long b7) {
        this.minApi = minApi;
        this.maxApi = maxApi;
        this.magic = b7 << 56 | b6 << 48 | b5 << 40 | b4 << 32 | b3 << 24 | b2 << 16 | b1 << 8 | b0;
    }

    public long getMagic() {
        return magic;
    }

    public byte[] getMagicArray() {
        return new byte[]{
                (byte) (magic),
                (byte) (magic >> 8),
                (byte) (magic >> 16),
                (byte) (magic >> 24),
                (byte) (magic >> 32),
                (byte) (magic >> 40),
                (byte) (magic >> 48),
                (byte) (magic >> 56)
        };
    }

    public boolean isCompact() {
        return this == CDEX001;
    }

    public boolean isDexContainer() {
        return this == DEX041;
    }

    public int getMinApi() {
        return minApi;
    }

    public int getMaxApi() {
        return maxApi;
    }

    public void checkApi(int api) {
        if (api < minApi) {
            throw new IllegalArgumentException(String.format(
                    "Target api(%d) is less than required(%d) for %s", api, minApi, this));
        }
        if (api > maxApi) {
            throw new IllegalArgumentException(String.format(
                    "Target api(%d) is greater than maximum(%d) for %s", api, minApi, this));
        }
    }

    public boolean isStandart() {
        return this != CDEX001 && this != DEX036;
    }

    public static DexVersion forApi(int api) {
        for (DexVersion version : values()) {
            if (version.isStandart() && api >= version.minApi) {
                return version;
            }
        }
        throw new IllegalArgumentException("Can`t find DexVersion for api " + api);
    }

    public static DexVersion forMagic(long magic) {
        for (DexVersion version : values()) {
            if (magic == version.magic) {
                return version;
            }
        }

        throw new IllegalArgumentException(
                String.format("Unknown dex magic: %016X", magic));
    }
}
