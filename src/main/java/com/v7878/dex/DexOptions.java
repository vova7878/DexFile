package com.v7878.dex;

import static com.v7878.dex.DexConstants.API_M3;

import com.v7878.misc.Version;

public abstract sealed class DexOptions<D extends DexOptions<D>> permits ReadOptions, WriteOptions {
    private static final int MIN_TARGET_API = API_M3;
    private static final int MAX_TARGET_API = 37;
    private static final int FIRST_ART_TARGET = 19;
    private static final int LAST_DALVIK_TARGET = 20;
    private static final int FIRST_HIDDEN_API_TARGET = 28;

    protected final int api;
    protected final boolean art;
    protected final boolean odex;
    protected final boolean expanded;
    protected final boolean hiddenapi;
    protected final boolean debug_info;

    DexOptions(int api, boolean art, boolean odex, boolean expanded,
               boolean hiddenapi, boolean debug_info) {
        this.api = api;
        this.art = art;
        this.odex = odex;
        this.expanded = expanded;
        this.hiddenapi = hiddenapi;
        this.debug_info = debug_info;
    }

    DexOptions() {
        boolean is_android = Version.IS_ANDROID;
        int api = is_android ? Version.getSDK() : 26;
        if (api > MAX_TARGET_API) {
            api = MAX_TARGET_API;
        }
        this.api = api;
        this.art = api > LAST_DALVIK_TARGET;
        this.odex = is_android;
        this.expanded = 14 <= api && api <= 15;
        this.hiddenapi = false;
        this.debug_info = true;
    }

    public void validate() {
        if (api < MIN_TARGET_API || api > MAX_TARGET_API) {
            throw new IllegalArgumentException("Unsupported target api: " + api);
        }
        if ((art && api < FIRST_ART_TARGET) || (!art && api > LAST_DALVIK_TARGET)) {
            throw new IllegalArgumentException("Unsupported " + art + " art option for api " + api);
        }
        if (hiddenapi && (api < FIRST_HIDDEN_API_TARGET)) {
            throw new IllegalArgumentException("Unsupported hiddenapi option for api " + api);
        }
        if (expanded && !(14 <= api && api <= 15)) {
            throw new IllegalArgumentException("Unsupported expanded opcodes option for api " + api);
        }
    }

    protected abstract D dup(int api, boolean art, boolean odex,
                             boolean expanded_opcodes,
                             boolean hiddenapi, boolean debug_info);

    public int getTargetApi() {
        return api;
    }

    public D withTargetApi(int api) {
        return dup(api, art, odex, expanded, hiddenapi, debug_info);
    }

    public boolean isTargetForArt() {
        return art;
    }

    public D withTargetForArt(boolean art) {
        return dup(api, art, odex, expanded, hiddenapi, debug_info);
    }

    public boolean isTargetForDalvik() {
        return !art;
    }

    public boolean hasOdexInstructions() {
        return odex;
    }

    public D withOdexInstructions(boolean odex) {
        return dup(api, art, odex, expanded, hiddenapi, debug_info);
    }

    public boolean hasExpandedInstructions() {
        return expanded;
    }

    public D withExpandedInstructions(boolean expanded) {
        return dup(api, art, odex, expanded, hiddenapi, debug_info);
    }

    public boolean hasHiddenApiFlags() {
        return hiddenapi;
    }

    public D withHiddenApiFlags(boolean hiddenapi) {
        return dup(api, art, odex, expanded, hiddenapi, debug_info);
    }

    public boolean hasDebugInfo() {
        return debug_info;
    }

    public D withDebugInfo(boolean debug_info) {
        return dup(api, art, odex, expanded, hiddenapi, debug_info);
    }
}
