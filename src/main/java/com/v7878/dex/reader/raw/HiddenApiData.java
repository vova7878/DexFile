package com.v7878.dex.reader.raw;

import static com.v7878.dex.DexConstants.NO_OFFSET;

import com.v7878.dex.reader.ReaderDex;

import java.util.function.IntSupplier;

public class HiddenApiData {
    private static final IntSupplier ZERO = () -> 0;

    public static final int OFFSETS_LIST_OFFSET = 4;
    public static final int OFFSET_ITEM_SIZE = 4;

    private final ReaderDex dexfile;
    private final int section_offset;

    public HiddenApiData(ReaderDex dexfile, int section_offset) {
        this.dexfile = dexfile;
        this.section_offset = section_offset;
    }

    public IntSupplier iterator(int class_idx) {
        if (section_offset == NO_OFFSET) {
            return ZERO;
        }
        int flags_offset = dexfile.dataAt(section_offset +
                OFFSETS_LIST_OFFSET + class_idx * OFFSET_ITEM_SIZE).readSmallUInt();
        if (flags_offset == NO_OFFSET) {
            return ZERO;
        }
        var in = dexfile.dataAt(section_offset + flags_offset);
        return in::readULeb128;
    }
}
