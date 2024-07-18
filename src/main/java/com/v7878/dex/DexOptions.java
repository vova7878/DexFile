/*
 * Copyright (c) 2023 Vladimir Kozelkov
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.v7878.dex;

import static com.v7878.misc.Version.CORRECT_SDK_INT;

public abstract class DexOptions<D extends DexOptions<D>> {
    private static final int MIN_TARGET_API = 1;
    private static final int MAX_TARGET_API = 34;
    private static final int FIRST_ART_TARGET = 19;
    private static final int LAST_DALVIK_TARGET = 20; // TODO: is this really true?

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
            api = 26;
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

    void requireMinApi(int api) {
        if (targetApi < api) {
            throw new IllegalArgumentException("Requested minimum API level " + api + " but target is " + targetApi);
        }
    }
}
