package com.v7878.dex.reader.raw;

import com.v7878.dex.reader.ReaderAnnotation;
import com.v7878.dex.reader.ReaderDex;
import com.v7878.dex.reader.util.CachedFixedSizeList;

public class AnnotationList extends CachedFixedSizeList<ReaderAnnotation> {
    public static final int SIZE_OFFSET = 0;
    public static final int LIST_OFFSET = 4;

    private final ReaderDex dexfile;
    private final int data_offset;

    private AnnotationList(ReaderDex dexfile, int size, int data_offset) {
        super(size);
        this.dexfile = dexfile;
        this.data_offset = data_offset;
    }

    @Override
    protected ReaderAnnotation compute(int index) {
        return dexfile.getAnnotation(dexfile.dataAt(
                data_offset + index * Integer.BYTES).readSmallUInt());
    }

    public static AnnotationList readItem(ReaderDex dexfile, int offset) {
        int size = dexfile.dataAt(offset + SIZE_OFFSET).readSmallUInt();
        return new AnnotationList(dexfile, size, offset + LIST_OFFSET);
    }
}
