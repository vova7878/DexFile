package com.v7878.dex;

public final class ReadOptions extends DexOptions {
    //TODO: public static final int ALLOW_CDEX_READING = 1 << 2;
    //TODO: public static final int MERGE_DEX_CONTAINERS = 1 << 3;
    private static final int ALL_READ_FLAGS = 0;

    ReadOptions(int targetApi, int flags) {
        super(targetApi, flags & ~ALL_READ_FLAGS);
    }

    private ReadOptions() {
        super();
    }

    public static ReadOptions of(int targetApi, int flags) {
        return new ReadOptions(targetApi, flags);
    }

    public static ReadOptions defaultOptions() {
        return new ReadOptions();
    }
}
