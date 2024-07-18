package com.v7878.dex;

public final class ReadOptions extends DexOptions<ReadOptions> {
    //TODO: public static final int ALLOW_CDEX_READING = 1 << 2;
    //TODO: public static final int MERGE_DEX_CONTAINERS = 1 << 3;

    ReadOptions(int targetApi, boolean targetForArt, boolean allowOdexInstructions) {
        super(targetApi, targetForArt, allowOdexInstructions);
    }

    private ReadOptions() {
        super();
    }

    @Override
    protected ReadOptions dup(int targetApi, boolean targetForArt, boolean allowOdexInstructions) {
        return new ReadOptions(targetApi, targetForArt, allowOdexInstructions);
    }

    public static ReadOptions defaultOptions() {
        return new ReadOptions();
    }
}
