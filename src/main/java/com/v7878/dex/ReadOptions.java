package com.v7878.dex;

public final class ReadOptions extends DexOptions<ReadOptions> {
    ReadOptions(int api, boolean art, boolean odex, boolean hiddenapi) {
        super(api, art, odex, hiddenapi);
    }

    private ReadOptions() {
        super();
    }

    @Override
    protected ReadOptions dup(int api, boolean art, boolean odex, boolean hiddenapi) {
        return new ReadOptions(api, art, odex, hiddenapi);
    }

    public static ReadOptions defaultOptions() {
        return new ReadOptions();
    }

    //TODO: verify checksum/signature option
}