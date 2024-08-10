package com.v7878.dex.reader.raw;

import com.v7878.dex.reader.ReaderDex;
import com.v7878.dex.reader.ReaderTypeId;
import com.v7878.dex.reader.util.CachedFixedSizeList;

public class TypeList extends CachedFixedSizeList<ReaderTypeId> {
    public static final int SIZE_OFFSET = 0;
    public static final int LIST_OFFSET = 4;

    private final ReaderDex dexfile;
    private final int data_offset;

    private TypeList(ReaderDex dexfile, int size, int data_offset) {
        super(size);
        this.dexfile = dexfile;
        this.data_offset = data_offset;
    }

    @Override
    protected ReaderTypeId compute(int index) {
        return dexfile.getTypeId(dexfile.dataAt(data_offset + index * Short.BYTES).readUShort());
    }

    public static TypeList readItem(ReaderDex dexfile, int offset) {
        int size = dexfile.dataAt(offset + SIZE_OFFSET).readSmallUInt();
        return new TypeList(dexfile, size, offset + LIST_OFFSET);
    }
}
