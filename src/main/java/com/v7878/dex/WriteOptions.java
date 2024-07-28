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

    //TODO: withRedirectedDataBase(int offset /*from main section*/, RandomOutput)
    //TODO: withCDEXFlags(int flags)
}
