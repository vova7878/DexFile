package com.v7878.dex;

import static com.v7878.misc.Version.CORRECT_SDK_INT;

public abstract class DexOptions<D extends DexOptions<D>> {
    private static final int MIN_TARGET_API = 1;
    private static final int MAX_TARGET_API = 35;
    private static final int FIRST_ART_TARGET = 19;
    private static final int LAST_DALVIK_TARGET = 20;

    protected final int targetApi;
    protected final boolean targetForArt;
    protected final boolean allowOdexInstructions;

    DexOptions(int targetApi, boolean targetForArt, boolean allowOdexInstructions) {
        if (targetApi < MIN_TARGET_API || targetApi > MAX_TARGET_API) {
            throw new IllegalArgumentException("unsupported target api: " + targetApi);
        }
        if ((targetForArt && targetApi < FIRST_ART_TARGET) || (!targetForArt && targetApi > LAST_DALVIK_TARGET)) {
            throw new IllegalArgumentException("unsupported " + targetForArt + " targetArt option with targetApi: " + targetApi);
        }
        this.targetApi = targetApi;
        this.targetForArt = targetForArt;
        this.allowOdexInstructions = allowOdexInstructions;
    }

    @SuppressWarnings("ConstantValue")
    DexOptions() {
        boolean is_android;
        int api;
        try {
            api = CORRECT_SDK_INT;
            is_android = true;
        } catch (Throwable th) {
            api = FIRST_ART_TARGET;
            is_android = false;
        }
        this.targetApi = api;
        this.targetForArt = api >= FIRST_ART_TARGET;
        this.allowOdexInstructions = is_android;
    }

    protected abstract D dup(int targetApi, boolean targetForArt, boolean allowOdexInstructions);

    public int getTargetApi() {
        return targetApi;
    }

    public D withTargetApi(int targetApi) {
        return dup(targetApi, targetForArt, allowOdexInstructions);
    }

    public boolean isTargetForArt() {
        return targetForArt;
    }

    public D withTargetForArt(boolean targetForArt) {
        return dup(targetApi, targetForArt, allowOdexInstructions);
    }

    public boolean isTargetForDalvik() {
        return !targetForArt;
    }

    public boolean hasOdexInstructions() {
        return allowOdexInstructions;
    }

    public D withOdexInstructions(boolean allowOdexInstructions) {
        return dup(targetApi, targetForArt, allowOdexInstructions);
    }
}
