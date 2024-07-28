/*
 * Copyright (c) 2024 Vladimir Kozelkov
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

import com.v7878.dex.io.RandomInput;

public final class ReadOptions extends DexOptions<ReadOptions> {
    //TODO: public static final int MERGE_DEX_CONTAINERS = 1 << 3;

    private final boolean lazyReading;
    private final RandomInput data_base;

    ReadOptions(int targetApi, boolean targetForArt, boolean allowOdexInstructions,
                boolean lazyReading, RandomInput data_base) {
        super(targetApi, targetForArt, allowOdexInstructions);
        this.lazyReading = lazyReading;
        this.data_base = data_base;
    }

    private ReadOptions() {
        super();
        this.data_base = null;
        this.lazyReading = true;
    }

    @Override
    protected ReadOptions dup(int targetApi, boolean targetForArt, boolean allowOdexInstructions) {
        return new ReadOptions(targetApi, targetForArt, allowOdexInstructions, lazyReading, data_base);
    }

    public RandomInput getRedirectedDataBase() {
        return data_base;
    }

    public ReadOptions withRedirectedDataBase(RandomInput data_base) {
        return new ReadOptions(targetApi, targetForArt, allowOdexInstructions, lazyReading, data_base);
    }

    public boolean isLazyReading() {
        return lazyReading;
    }

    public ReadOptions withLazyReading(boolean lazyReading) {
        return new ReadOptions(targetApi, targetForArt, allowOdexInstructions, lazyReading, data_base);
    }

    public static ReadOptions defaultOptions() {
        return new ReadOptions();
    }
}
