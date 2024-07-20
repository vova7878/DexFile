package com.v7878.dex;

public final class WriteOptions extends DexOptions<WriteOptions> {
    //TODO: public static final int ALLOW_SPLITTING_INTO_DEX_CONTAINERS = 1 << 3;

    private final DexVersion version;

    WriteOptions(DexVersion version, int targetApi, boolean targetForArt, boolean allowOdexInstructions) {
        super(targetApi, targetForArt, allowOdexInstructions);
        requireMinApi(version.getMinApi());
        this.version = version;
    }

    private WriteOptions() {
        super();
        this.version = DexVersion.forApi(targetApi);
    }

    @Override
    protected WriteOptions dup(int targetApi, boolean targetForArt, boolean allowOdexInstructions) {
        return new WriteOptions(version, targetApi, targetForArt, allowOdexInstructions);
    }

    public DexVersion getDexVersion() {
        return version;
    }

    public WriteOptions withDexVersion(DexVersion version) {
        return new WriteOptions(version, targetApi, targetForArt, allowOdexInstructions);
    }

    public static WriteOptions defaultOptions() {
        return new WriteOptions();
    }
}
