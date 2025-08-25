package com.v7878.dex.raw;

import com.v7878.dex.DexIO.DexMapEntry;

public record MapItem(int type, int size, int offset) implements DexMapEntry {
}
