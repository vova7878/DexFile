package com.v7878.dex;

public final class WriteOptions extends DexOptions {
    //TODO: public static final int WRITE_CDEX = 1 << 2;
    //TODO: public static final int ALLOW_SPLITTING_INTO_DEX_CONTAINERS = 1 << 3;
    private static final int ALL_WRITE_FLAGS = 0;

    private WriteOptions(int targetApi, int flags) {
        super(targetApi, flags & ~ALL_WRITE_FLAGS);
    }

    private WriteOptions() {
        super();
    }

    public static WriteOptions of(int targetApi, int flags) {
        return new WriteOptions(targetApi, flags);
    }

    public static WriteOptions defaultOptions() {
        return new WriteOptions();
    }
}
