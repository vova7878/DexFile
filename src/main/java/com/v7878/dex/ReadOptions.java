package com.v7878.dex;

public final class ReadOptions extends DexOptions<ReadOptions> {
    ReadOptions(int api, boolean art, boolean odex, boolean hiddenapi, boolean debug_info) {
        super(api, art, odex, hiddenapi, debug_info);
    }

    private ReadOptions() {
        super();
    }

    @Override
    protected ReadOptions dup(int api, boolean art, boolean odex, boolean hiddenapi, boolean debug_info) {
        return new ReadOptions(api, art, odex, hiddenapi, debug_info);
    }

    public static ReadOptions defaultOptions() {
        return new ReadOptions();
    }

    //TODO: verify checksum/signature option
}