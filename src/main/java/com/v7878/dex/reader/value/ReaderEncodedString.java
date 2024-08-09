package com.v7878.dex.reader.value;

import com.v7878.dex.base.value.BaseEncodedString;
import com.v7878.dex.io.RandomInput;
import com.v7878.dex.io.ValueCoder;
import com.v7878.dex.reader.ReaderDex;

public class ReaderEncodedString extends BaseEncodedString {
    private final String value;

    private ReaderEncodedString(String value) {
        this.value = value;
    }

    public static ReaderEncodedString readValue(ReaderDex dexfile, RandomInput in, int arg) {
        return new ReaderEncodedString(dexfile.getString(
                ValueCoder.readUnsignedInt(in, arg, false)));
    }

    @Override
    public String getValue() {
        return value;
    }
}
