package com.v7878.dex;

import static com.v7878.misc.Version.CORRECT_SDK_INT;

public abstract sealed class DexOptions<D extends DexOptions<D>> permits ReadOptions, WriteOptions {
    private static final int MIN_TARGET_API = 1;
    private static final int MAX_TARGET_API = 36;
    private static final int FIRST_ART_TARGET = 19;
    private static final int LAST_DALVIK_TARGET = 20;
    private static final int FIRST_HIDDEN_API_TARGET = 28;

    protected final int api;
    protected final boolean art;
    protected final boolean odex;
    protected final boolean hiddenapi;

    DexOptions(int api, boolean art, boolean odex, boolean hiddenapi) {
        this.api = api;
        this.art = art;
        this.odex = odex;
        this.hiddenapi = hiddenapi;
    }

    @SuppressWarnings("ConstantValue")
    DexOptions() {
        boolean is_android;
        int api;
        try {
            api = CORRECT_SDK_INT;
            is_android = true;
        } catch (Throwable th) {
            api = 26;
            is_android = false;
        }
        this.api = api;
        this.art = api > LAST_DALVIK_TARGET;
        this.odex = is_android;
        this.hiddenapi = false;
    }

    public void validate() {
        if (api < MIN_TARGET_API || api > MAX_TARGET_API) {
            throw new IllegalArgumentException("Unsupported target api: " + api);
        }
        if ((art && api < FIRST_ART_TARGET) || (!art && api > LAST_DALVIK_TARGET)) {
            throw new IllegalArgumentException("Unsupported " + art + " targetArt option with targetApi: " + api);
        }
        if (hiddenapi && (api < FIRST_HIDDEN_API_TARGET)) {
            throw new IllegalArgumentException("Unsupported " + art + " hiddenapi option with targetApi: " + api);
        }
    }

    protected abstract D dup(int api, boolean art, boolean odex, boolean hiddenapi);

    public int getTargetApi() {
        return api;
    }

    public D withTargetApi(int targetApi) {
        return dup(targetApi, art, odex, hiddenapi);
    }

    public boolean isTargetForArt() {
        return art;
    }

    public D withTargetForArt(boolean targetForArt) {
        return dup(api, targetForArt, odex, hiddenapi);
    }

    public boolean isTargetForDalvik() {
        return !art;
    }

    public boolean hasOdexInstructions() {
        return odex;
    }

    public D withOdexInstructions(boolean allowOdexInstructions) {
        return dup(api, art, allowOdexInstructions, hiddenapi);
    }

    public boolean hasHiddenApiFlags() {
        return hiddenapi;
    }

    public D withHiddenApiFlags(boolean hiddenapi) {
        return dup(api, art, odex, hiddenapi);
    }
}
