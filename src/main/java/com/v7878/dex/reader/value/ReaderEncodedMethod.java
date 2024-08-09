package com.v7878.dex.reader.value;

import com.v7878.dex.base.value.BaseEncodedMethod;
import com.v7878.dex.io.RandomInput;
import com.v7878.dex.io.ValueCoder;
import com.v7878.dex.reader.ReaderDex;
import com.v7878.dex.reader.ReaderMethodId;

public class ReaderEncodedMethod extends BaseEncodedMethod {
    private final ReaderMethodId value;

    private ReaderEncodedMethod(ReaderMethodId value) {
        this.value = value;
    }

    public static ReaderEncodedMethod readValue(ReaderDex dexfile, RandomInput in, int arg) {
        return new ReaderEncodedMethod(dexfile.getMethodId(
                ValueCoder.readUnsignedInt(in, arg, false)));
    }

    @Override
    public ReaderMethodId getValue() {
        return value;
    }
}
