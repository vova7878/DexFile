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

public abstract class DexOptions {
    public static final int TARGET_FOR_ART = 1;
    public static final int ALLOW_ODEX_INSTRUCTIONS = 1 << 1;
    private static final int ALL_FLAGS = TARGET_FOR_ART | ALLOW_ODEX_INSTRUCTIONS;

    private static final int MIN_TARGET_API = 1;
    private static final int MAX_TARGET_API = 34;
    private static final int FIRST_ART_TARGET = 19;
    private static final int LAST_DALVIK_TARGET = 20; // TODO: is this really true?

    private final int targetApi;
    private final boolean targetForArt;
    private final boolean allowOdexInstructions;

    DexOptions(int targetApi, int flags) {
        if (targetApi < MIN_TARGET_API || targetApi > MAX_TARGET_API) {
            throw new IllegalArgumentException("unsupported target api: " + targetApi);
        }
        if ((flags & ~ALL_FLAGS) != 0) {
            throw new IllegalArgumentException("unsupported flags " + flags);
        }
        this.targetApi = targetApi;
        this.targetForArt = (flags & TARGET_FOR_ART) != 0;
        this.allowOdexInstructions = (flags & ALLOW_ODEX_INSTRUCTIONS) != 0;
        if ((targetForArt && targetApi < FIRST_ART_TARGET) || (!targetForArt && targetApi > LAST_DALVIK_TARGET)) {
            throw new IllegalArgumentException("unsupported " + targetForArt + " targetArt option with targetApi: " + targetApi);
        }
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

    public int getTargetApi() {
        return targetApi;
    }

    public boolean isTargetForArt() {
        return targetForArt;
    }

    public boolean isTargetForDalvik() {
        return !targetForArt;
    }

    public boolean hasOdexInstructions() {
        return allowOdexInstructions;
    }

    public void requireMinApi(int minApi) {
        if (targetApi < minApi) {
            //TODO: message
            throw new IllegalArgumentException();
        }
    }

    public void requireMaxApi(int maxApi) {
        if (targetApi > maxApi) {
            //TODO: message
            throw new IllegalArgumentException();
        }
    }

    public void requireForArt() {
        if (!targetForArt) {
            //TODO: message
            throw new IllegalArgumentException();
        }
    }

    public void requireForDalvik() {
        if (targetForArt) {
            //TODO: message
            throw new IllegalArgumentException();
        }
    }

    public void requireOdexInstructions() {
        if (!allowOdexInstructions) {
            //TODO: message
            throw new IllegalArgumentException();
        }
    }
}
