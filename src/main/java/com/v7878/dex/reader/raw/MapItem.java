package com.v7878.dex.reader.raw;

import com.v7878.dex.reader.ReaderDex;

public class MapItem {
    public static final int ITEM_SIZE = 12;

    public static final int TYPE_OFFSET = 0;
    public static final int SIZE_OFFSET = 4;
    public static final int OFFSET_OFFSET = 8;

    private final ReaderDex dexfile;
    private final int offset;

    public MapItem(ReaderDex dexfile, int offset) {
        this.dexfile = dexfile;
        this.offset = offset;
    }

    public int getType() {
        return dexfile.dataAt(offset + TYPE_OFFSET).readUShort();
    }

    public int getItemCount() {
        return dexfile.dataAt(offset + SIZE_OFFSET).readSmallUInt();
    }

    public int getOffset() {
        return dexfile.dataAt(offset + OFFSET_OFFSET).readSmallUInt();
    }
}
