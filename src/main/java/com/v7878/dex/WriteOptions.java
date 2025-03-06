package com.v7878.dex;

public final class WriteOptions extends DexOptions<WriteOptions> {
    private final DexVersion dex_version;

    WriteOptions(DexVersion dex_version, int api, boolean art, boolean odex, boolean hiddenapi) {
        super(api, art, odex, hiddenapi);
        this.dex_version = dex_version;
    }

    private WriteOptions() {
        super();
        this.dex_version = DexVersion.forApi(api);
    }

    @Override
    public void validate() {
        //TODO: requireMinApi(dex_version.getMinApi());
        super.validate();
    }

    @Override
    protected WriteOptions dup(int api, boolean art, boolean odex, boolean hiddenapi) {
        return new WriteOptions(dex_version, api, art, odex, hiddenapi);
    }

    public DexVersion getDexVersion() {
        return dex_version;
    }

    public WriteOptions withDexVersion(DexVersion version) {
        return new WriteOptions(version, api, art, odex, hiddenapi);
    }

    public static WriteOptions defaultOptions() {
        return new WriteOptions();
    }

    //TODO: withCDEXFlags(int flags)
    //TODO: setByteOrder (see https://android.googlesource.com/platform/dalvik/+/kitkat-mr2-release/libdex/DexSwapVerify.cpp)
}
