package com.v7878.dex;

import java.util.Objects;

public final class WriteOptions extends DexOptions<WriteOptions> {
    public enum StringFix {
        NONE, FIX_JUMBO, FIX_ALL
    }

    public enum RawFix {
        DO_NOT_TOUCH, USE_WRAPPER, USE_RAW
    }

    private final DexVersion dex_version;

    private final StringFix string_fix;

    private final RawFix raw_fix;
    private final Integer compact_flags;
    private final boolean sort_defs;

    WriteOptions(DexVersion dex_version, StringFix string_fix,
                 RawFix raw_fix, Integer compact_flags,
                 boolean sort_defs, int api, boolean art,
                 boolean odex, boolean expanded_opcodes,
                 boolean hiddenapi, boolean debug_info) {
        super(api, art, odex, expanded_opcodes, hiddenapi, debug_info);
        this.dex_version = Objects.requireNonNull(dex_version);
        this.string_fix = Objects.requireNonNull(string_fix);
        this.raw_fix = Objects.requireNonNull(raw_fix);
        this.compact_flags = compact_flags;
        this.sort_defs = sort_defs;
    }

    private WriteOptions() {
        super();
        this.dex_version = DexVersion.forApi(api);
        this.string_fix = StringFix.FIX_JUMBO;
        this.raw_fix = RawFix.DO_NOT_TOUCH;
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
    protected WriteOptions dup(int api, boolean art, boolean odex,
                               boolean expanded_opcodes,
                               boolean hiddenapi, boolean debug_info) {
        return new WriteOptions(dex_version, string_fix, raw_fix, compact_flags,
                sort_defs, api, art, odex, expanded_opcodes, hiddenapi, debug_info);
    }

    public DexVersion getDexVersion() {
        return dex_version;
    }

    public WriteOptions withDexVersion(DexVersion version) {
        return new WriteOptions(version, string_fix, raw_fix, compact_flags,
                sort_defs, api, art, odex, expanded, hiddenapi, debug_info);
    }

    public Integer getCDEXFlags() {
        return compact_flags;
    }

    public WriteOptions withCDEXFlags(Integer flags) {
        return new WriteOptions(dex_version, string_fix, raw_fix, flags,
                sort_defs, api, art, odex, expanded, hiddenapi, debug_info);
    }

    public boolean isClassSorting() {
        return sort_defs;
    }

    public WriteOptions withClassSorting(boolean sort) {
        return new WriteOptions(dex_version, string_fix, raw_fix, compact_flags,
                sort, api, art, odex, expanded, hiddenapi, debug_info);
    }

    public StringFix getStringFix() {
        return string_fix;
    }

    public WriteOptions withStringFix(StringFix string_fix) {
        return new WriteOptions(dex_version, string_fix, raw_fix, compact_flags,
                sort_defs, api, art, odex, expanded, hiddenapi, debug_info);
    }

    public RawFix getRawFix() {
        return raw_fix;
    }

    public WriteOptions withRawFix(RawFix raw_fix) {
        return new WriteOptions(dex_version, string_fix, raw_fix, compact_flags,
                sort_defs, api, art, odex, expanded, hiddenapi, debug_info);
    }

    public static WriteOptions defaultOptions() {
        return new WriteOptions();
    }
}
