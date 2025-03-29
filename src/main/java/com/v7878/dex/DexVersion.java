package com.v7878.dex;

public enum DexVersion {
    DEX041(35, 'd', 'e', 'x', '\n', '0', '4', '1', '\0'),
    DEX040(30, 'd', 'e', 'x', '\n', '0', '4', '0', '\0'),
    DEX039(28, 'd', 'e', 'x', '\n', '0', '3', '9', '\0'),
    DEX038(26, 'd', 'e', 'x', '\n', '0', '3', '8', '\0'),
    DEX037(24, 'd', 'e', 'x', '\n', '0', '3', '7', '\0'),
    DEX035(1, 'd', 'e', 'x', '\n', '0', '3', '5', '\0'),
    CDEX001(28, 'c', 'd', 'e', 'x', '0', '0', '1', '\0');

    private final int minApi;
    private final long magic;

    DexVersion(int minApi, long b0, long b1, long b2, long b3, long b4, long b5, long b6, long b7) {
        this.minApi = minApi;
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

    public static DexVersion forApi(int api) {
        for (DexVersion version : values()) {
            if (api >= version.minApi) {
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
