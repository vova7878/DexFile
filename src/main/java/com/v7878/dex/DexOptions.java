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
    protected final boolean debug_info;

    DexOptions(int api, boolean art, boolean odex, boolean hiddenapi, boolean debug_info) {
        this.api = api;
        this.art = art;
        this.odex = odex;
        this.hiddenapi = hiddenapi;
        this.debug_info = debug_info;
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
        this.debug_info = true;
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

    protected abstract D dup(int api, boolean art, boolean odex, boolean hiddenapi, boolean debug_info);

    public int getTargetApi() {
        return api;
    }

    public D withTargetApi(int api) {
        return dup(api, art, odex, hiddenapi, debug_info);
    }

    public boolean isTargetForArt() {
        return art;
    }

    public D withTargetForArt(boolean art) {
        return dup(api, art, odex, hiddenapi, debug_info);
    }

    public boolean isTargetForDalvik() {
        return !art;
    }

    public boolean hasOdexInstructions() {
        return odex;
    }

    public D withOdexInstructions(boolean odex) {
        return dup(api, art, odex, hiddenapi, debug_info);
    }

    public boolean hasHiddenApiFlags() {
        return hiddenapi;
    }

    public D withHiddenApiFlags(boolean hiddenapi) {
        return dup(api, art, odex, hiddenapi, debug_info);
    }

    public boolean hasDebugInfo() {
        return debug_info;
    }

    public D withDebugInfo(boolean debug_info) {
        return dup(api, art, odex, hiddenapi, debug_info);
    }
}
