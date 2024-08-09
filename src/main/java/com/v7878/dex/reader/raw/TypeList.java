package com.v7878.dex.reader.raw;

import com.v7878.dex.reader.ReaderDex;
import com.v7878.dex.reader.ReaderTypeId;
import com.v7878.dex.reader.util.CachedFixedSizeList;

public class TypeList extends CachedFixedSizeList<ReaderTypeId> {
    public static final int SIZE_OFFSET = 0;
    public static final int LIST_OFFSET = 4;

    private final ReaderDex dexfile;
    private final int list_offset;

    private TypeList(ReaderDex dexfile, int size, int list_offset) {
        super(size);
        this.dexfile = dexfile;
        this.list_offset = list_offset;
    }

    @Override
    protected ReaderTypeId compute(int index) {
        return dexfile.getTypeId(dexfile.dataAt(list_offset + index * Short.BYTES).readUShort());
    }

    public static TypeList readItem(ReaderDex dexfile, int item_offset) {
        int size = dexfile.dataAt(item_offset + SIZE_OFFSET).readSmallUInt();
        return new TypeList(dexfile, size, item_offset + LIST_OFFSET);
    }
}
