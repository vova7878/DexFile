package com.v7878.dex.reader.value;

import com.v7878.dex.base.value.BaseEncodedArray;
import com.v7878.dex.iface.value.EncodedValue;
import com.v7878.dex.io.RandomInput;
import com.v7878.dex.reader.ReaderDex;
import com.v7878.dex.reader.util.CachedVariableSizeList;

import java.util.ArrayList;
import java.util.List;

public class ReaderEncodedArray extends BaseEncodedArray {
    private final List<? extends EncodedValue> value;

    private ReaderEncodedArray(List<? extends EncodedValue> value) {
        this.value = value;
    }

    private static ReaderEncodedArray readValue(ReaderDex dexfile, RandomInput in, boolean lazy) {
        List<EncodedValue> value;
        int size = in.readSmallULeb128();
        if (size == 0) {
            value = List.of();
        } else if (lazy) {
            value = new CachedVariableSizeList<>(size) {
                @Override
                protected EncodedValue computeNext() {
                    // TODO: what if exception?
                    return EncodedValueReader.readValue(dexfile, in);
                }
            };
        } else {
            value = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                value.add(i, EncodedValueReader.readValue(dexfile, in));
            }
        }
        return new ReaderEncodedArray(value);
    }

    public static ReaderEncodedArray readValue(ReaderDex dexfile, RandomInput in) {
        return readValue(dexfile, in, false);
    }

    public static ReaderEncodedArray readValue(ReaderDex dexfile, int offset) {
        return readValue(dexfile, dexfile.dataAt(offset), true);
    }

    @Override
    public List<? extends EncodedValue> getValue() {
        return value;
    }
}
