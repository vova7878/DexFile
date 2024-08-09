package com.v7878.dex.reader;

import com.v7878.dex.base.BaseTypeId;

public class ReaderTypeId extends BaseTypeId {
    public static int ITEM_SIZE = 4;

    private final ReaderDex dexfile;
    private final int offset;

    public ReaderTypeId(ReaderDex dexfile, int index, int type_ids_off) {
        this.dexfile = dexfile;
        this.offset = type_ids_off + index * ITEM_SIZE;
    }

    private String descriptor;

    @Override
    public String getDescriptor() {
        if (descriptor != null) return descriptor;
        return descriptor = dexfile.getString(dexfile.mainAt(offset).readSmallUInt());
    }
}
