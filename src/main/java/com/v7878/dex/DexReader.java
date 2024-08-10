package com.v7878.dex;

import com.v7878.dex.iface.Dex;
import com.v7878.dex.reader.ReaderDex;

public class DexReader {
    public static Dex loadDex(ReadOptions options, byte[] data) {
        return new ReaderDex(options, data);
    }
}
