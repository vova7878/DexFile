package com.v7878.dex;

import com.v7878.dex.io.RandomInput;

public final class ReadOptions extends DexOptions<ReadOptions> {
    //TODO: public static final int MERGE_DEX_CONTAINERS = 1 << 3;

    private final RandomInput data_base;

    ReadOptions(int targetApi, boolean targetForArt, boolean allowOdexInstructions, RandomInput data_base) {
        super(targetApi, targetForArt, allowOdexInstructions);
        this.data_base = data_base;
    }

    private ReadOptions() {
        super();
        this.data_base = null;
    }

    @Override
    protected ReadOptions dup(int targetApi, boolean targetForArt, boolean allowOdexInstructions) {
        return new ReadOptions(targetApi, targetForArt, allowOdexInstructions, data_base);
    }

    public RandomInput getRedirectedDataBase() {
        return data_base;
    }

    public ReadOptions withRedirectedDataBase(RandomInput data_base) {
        return new ReadOptions(targetApi, targetForArt, allowOdexInstructions, data_base);
    }

    public static ReadOptions defaultOptions() {
        return new ReadOptions();
    }
}
