package com.v7878.dex.reader.raw;

import static com.v7878.dex.DexConstants.NO_OFFSET;

import com.v7878.dex.reader.ReaderAnnotation;
import com.v7878.dex.reader.ReaderDex;
import com.v7878.dex.reader.util.CachedFixedSizeList;
import com.v7878.dex.reader.util.OptionalUtils;

import java.util.Set;

public class AnnotationSetList extends CachedFixedSizeList<Set<ReaderAnnotation>> {
    public static final int SIZE_OFFSET = 0;
    public static final int LIST_OFFSET = 4;

    private final ReaderDex dexfile;
    private final int data_offset;

    private AnnotationSetList(ReaderDex dexfile, int size, int data_offset) {
        super(size);
        this.dexfile = dexfile;
        this.data_offset = data_offset;
    }

    @Override
    protected Set<ReaderAnnotation> compute(int index) {
        int offset = dexfile.dataAt(data_offset + index * Integer.BYTES).readSmallUInt();
        return OptionalUtils.getOrDefault(offset, NO_OFFSET, dexfile::getAnnotationSet, Set.of());
    }

    public static AnnotationSetList readItem(ReaderDex dexfile, int offset) {
        int size = dexfile.dataAt(offset + SIZE_OFFSET).readSmallUInt();
        return new AnnotationSetList(dexfile, size, offset + LIST_OFFSET);
    }
}
