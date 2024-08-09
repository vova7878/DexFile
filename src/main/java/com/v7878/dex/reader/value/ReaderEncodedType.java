package com.v7878.dex.reader.value;

import com.v7878.dex.base.value.BaseEncodedType;
import com.v7878.dex.io.RandomInput;
import com.v7878.dex.io.ValueCoder;
import com.v7878.dex.reader.ReaderDex;
import com.v7878.dex.reader.ReaderTypeId;

public class ReaderEncodedType extends BaseEncodedType {
    private final ReaderTypeId value;

    private ReaderEncodedType(ReaderTypeId value) {
        this.value = value;
    }

    public static ReaderEncodedType readValue(ReaderDex dexfile, RandomInput in, int arg) {
        return new ReaderEncodedType(dexfile.getTypeId(
                ValueCoder.readUnsignedInt(in, arg, false)));
    }

    @Override
    public ReaderTypeId getValue() {
        return value;
    }
}
