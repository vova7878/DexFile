package com.v7878.dex.reader.raw;

import com.v7878.dex.reader.ReaderDex;

public class StringId {
    public static final int ITEM_SIZE = 4;

    public static String readItem(ReaderDex dexfile, int index, int string_ids_off) {
        int main_offset = string_ids_off + index * StringId.ITEM_SIZE;
        int data_offset = dexfile.mainAt(main_offset).readSmallUInt();
        return dexfile.dataAt(data_offset).readMUTF8();
    }
}
