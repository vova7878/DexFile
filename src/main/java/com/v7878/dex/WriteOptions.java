package com.v7878.dex;

import java.util.Objects;

public final class WriteOptions extends DexOptions<WriteOptions> {
    private final DexVersion dex_version;
    private final int compact_flags;

    WriteOptions(DexVersion dex_version, int compact_flags, int api, boolean art, boolean odex, boolean hiddenapi, boolean debug_info) {
        super(api, art, odex, hiddenapi, debug_info);
        this.dex_version = Objects.requireNonNull(dex_version);
        this.compact_flags = compact_flags;
    }

    private WriteOptions() {
        super();
        this.dex_version = DexVersion.forApi(api);
        this.compact_flags = 0;
    }

    @Override
    public void validate() {
        dex_version.checkApi(api);
        if (!dex_version.isCompact() && compact_flags != 0) {
            throw new IllegalArgumentException("Compact dex flags is not 0 for " + dex_version);
        }
        super.validate();
    }

    @Override
    protected WriteOptions dup(int api, boolean art, boolean odex, boolean hiddenapi, boolean debug_info) {
        return new WriteOptions(dex_version, compact_flags, api, art, odex, hiddenapi, debug_info);
    }

    public DexVersion getDexVersion() {
        return dex_version;
    }

    public WriteOptions withDexVersion(DexVersion version) {
        return new WriteOptions(version, compact_flags, api, art, odex, hiddenapi, debug_info);
    }

    public int getCDEXFlags() {
        return compact_flags;
    }

    public WriteOptions withCDEXFlags(int flags) {
        return new WriteOptions(dex_version, flags, api, art, odex, hiddenapi, debug_info);
    }

    public static WriteOptions defaultOptions() {
        return new WriteOptions();
    }
}
