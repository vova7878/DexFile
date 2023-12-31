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

//TODO: split into Read and Write Options
public class DexOptions {
    private static final int MIN_TARGET_API = 1;
    private static final int MAX_TARGET_API = 34;
    private static final int FIRST_ART_TARGET = 19;
    private static final int LAST_DALVIK_TARGET = 20; // TODO: ?

    private final int targetApi;
    private final boolean targetArt;
    private final boolean includeOdexInstructions;

    public DexOptions(int targetApi, boolean targetArt, boolean includeOdexInstructions) {
        if (targetApi < MIN_TARGET_API || targetApi > MAX_TARGET_API) {
            throw new IllegalArgumentException("unsupported target api: " + targetApi);
        }
        this.targetApi = targetApi;
        if ((targetArt && targetApi < FIRST_ART_TARGET) || (!targetArt && targetApi > LAST_DALVIK_TARGET)) {
            throw new IllegalArgumentException("unsupported " + targetArt + " targetArt option with targetApi: " + targetApi);
        }
        this.targetArt = targetArt;
        this.includeOdexInstructions = includeOdexInstructions;
    }

    public int getTargetApi() {
        return targetApi;
    }

    public boolean isTargetForArt() {
        return targetArt;
    }

    public boolean isTargetForDalvik() {
        return !targetArt;
    }

    public boolean includeOdexInstructions() {
        return includeOdexInstructions;
    }

    public static DexOptions defaultOptions() {
        try {
            return new DexOptions(CORRECT_SDK_INT, true, true);
        } catch (Throwable ignored) {
            // non-android host
            return new DexOptions(26, true, false);
        }
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
        if (!targetArt) {
            //TODO: message
            throw new IllegalArgumentException();
        }
    }

    public void requireForDalvik() {
        if (targetArt) {
            //TODO: message
            throw new IllegalArgumentException();
        }
    }

    public void requireOdexInstructions() {
        if (!includeOdexInstructions) {
            //TODO: message
            throw new IllegalArgumentException();
        }
    }
}
