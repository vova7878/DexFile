package com.v7878.dex;

public final class WriteOptions extends DexOptions<WriteOptions> {
    private final DexVersion dex_version;

    WriteOptions(DexVersion dex_version, int targetApi, boolean targetForArt, boolean allowOdexInstructions) {
        super(targetApi, targetForArt, allowOdexInstructions);
        //TODO: requireMinApi(dex_version.getMinApi());
        this.dex_version = dex_version;
    }

    private WriteOptions() {
        super();
        this.dex_version = DexVersion.forApi(targetApi);
    }

    @Override
    protected WriteOptions dup(int targetApi, boolean targetForArt, boolean allowOdexInstructions) {
        return new WriteOptions(dex_version, targetApi, targetForArt, allowOdexInstructions);
    }

    public DexVersion getDexVersion() {
        return dex_version;
    }

    public WriteOptions withDexVersion(DexVersion version) {
        return new WriteOptions(version, targetApi, targetForArt, allowOdexInstructions);
    }

    public static WriteOptions defaultOptions() {
        return new WriteOptions();
    }

    //TODO: withCDEXFlags(int flags)
    //TODO: skip hiddenapi writing option
}
