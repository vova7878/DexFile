package com.v7878.dex;

public final class WriteOptions extends DexOptions {
    private WriteOptions(int targetApi, boolean targetArt, boolean includeOdexInstructions) {
        super(targetApi, targetArt, includeOdexInstructions);
    }

    private WriteOptions() {
        super();
    }

    public static WriteOptions of(int targetApi, boolean targetArt, boolean includeOdexInstructions) {
        return new WriteOptions(targetApi, targetArt, includeOdexInstructions);
    }

    public static WriteOptions defaultOptions() {
        return new WriteOptions();
    }
}
