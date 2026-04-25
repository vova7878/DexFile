package com.v7878.dex;

import java.util.Objects;

public final class WriteOptions extends DexOptions<WriteOptions> {
    public enum StringFix {
        NONE, FIX_JUMBO, FIX_ALL
    }

    private final DexVersion dex_version;

    private final StringFix rewrite;
    private final Integer compact_flags;
    private final boolean sort_defs;

    WriteOptions(DexVersion dex_version, StringFix rewrite, Integer compact_flags, boolean sort_defs,
                 int api, boolean art, boolean odex, boolean hiddenapi, boolean debug_info) {
        super(api, art, odex, hiddenapi, debug_info);
        this.dex_version = Objects.requireNonNull(dex_version);
        this.rewrite = Objects.requireNonNull(rewrite);
        this.compact_flags = compact_flags;
        this.sort_defs = sort_defs;
    }

    private WriteOptions() {
        super();
        this.dex_version = DexVersion.forApi(api);
        this.rewrite = StringFix.FIX_JUMBO;
        this.compact_flags = null;
        this.sort_defs = false;
    }

    @Override
    public void validate() {
        dex_version.checkApi(api);
        if (!dex_version.isCompact() && compact_flags != null) {
            throw new IllegalArgumentException("Compact dex flags is not null for " + dex_version);
        }
        super.validate();
    }

    @Override
    protected WriteOptions dup(int api, boolean art, boolean odex, boolean hiddenapi, boolean debug_info) {
        return new WriteOptions(dex_version, rewrite, compact_flags, sort_defs, api, art, odex, hiddenapi, debug_info);
    }

    public DexVersion getDexVersion() {
        return dex_version;
    }

    public WriteOptions withDexVersion(DexVersion version) {
        return new WriteOptions(version, rewrite, compact_flags, sort_defs, api, art, odex, hiddenapi, debug_info);
    }

    public Integer getCDEXFlags() {
        return compact_flags;
    }

    public WriteOptions withCDEXFlags(Integer flags) {
        return new WriteOptions(dex_version, rewrite, flags, sort_defs, api, art, odex, hiddenapi, debug_info);
    }

    public boolean isClassSorting() {
        return sort_defs;
    }

    public WriteOptions withClassSorting(boolean sort) {
        return new WriteOptions(dex_version, rewrite, compact_flags, sort, api, art, odex, hiddenapi, debug_info);
    }

    public StringFix getStringFix() {
        return rewrite;
    }

    public WriteOptions withStringFix(StringFix rewrite) {
        return new WriteOptions(dex_version, rewrite, compact_flags, sort_defs, api, art, odex, hiddenapi, debug_info);
    }

    public static WriteOptions defaultOptions() {
        return new WriteOptions();
    }
}
