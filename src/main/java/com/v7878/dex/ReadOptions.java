package com.v7878.dex;

public final class ReadOptions extends DexOptions {
    ReadOptions(int targetApi, boolean targetArt, boolean includeOdexInstructions) {
        super(targetApi, targetArt, includeOdexInstructions);
    }

    private ReadOptions() {
        super();
    }

    public static ReadOptions of(int targetApi, boolean targetArt, boolean includeOdexInstructions) {
        return new ReadOptions(targetApi, targetArt, includeOdexInstructions);
    }

    public static ReadOptions defaultOptions() {
        return new ReadOptions();
    }
}
