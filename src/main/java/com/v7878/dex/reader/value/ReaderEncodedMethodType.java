package com.v7878.dex.reader.value;

import com.v7878.dex.base.value.BaseEncodedMethodType;
import com.v7878.dex.io.RandomInput;
import com.v7878.dex.io.ValueCoder;
import com.v7878.dex.reader.ReaderDex;
import com.v7878.dex.reader.ReaderProtoId;

public class ReaderEncodedMethodType extends BaseEncodedMethodType {
    private final ReaderProtoId value;

    private ReaderEncodedMethodType(ReaderProtoId value) {
        this.value = value;
    }

    public static ReaderEncodedMethodType readValue(ReaderDex dexfile, RandomInput in, int arg) {
        return new ReaderEncodedMethodType(dexfile.getProtoId(
                ValueCoder.readUnsignedInt(in, arg, false)));
    }

    @Override
    public ReaderProtoId getValue() {
        return value;
    }
}
