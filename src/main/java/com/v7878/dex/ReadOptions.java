package com.v7878.dex;

public final class ReadOptions extends DexOptions<ReadOptions> {
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

    //TODO: verify checksum/signature option
}