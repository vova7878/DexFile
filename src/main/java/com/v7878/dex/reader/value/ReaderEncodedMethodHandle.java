package com.v7878.dex.reader.value;

import com.v7878.dex.base.value.BaseEncodedMethodHandle;
import com.v7878.dex.io.RandomInput;
import com.v7878.dex.io.ValueCoder;
import com.v7878.dex.reader.ReaderDex;
import com.v7878.dex.reader.ReaderMethodHandleId;

public class ReaderEncodedMethodHandle extends BaseEncodedMethodHandle {
    private final ReaderMethodHandleId value;

    private ReaderEncodedMethodHandle(ReaderMethodHandleId value) {
        this.value = value;
    }

    public static ReaderEncodedMethodHandle readValue(ReaderDex dexfile, RandomInput in, int arg) {
        return new ReaderEncodedMethodHandle(dexfile.getMethodHandleId(
                ValueCoder.readUnsignedInt(in, arg, false)));
    }

    @Override
    public ReaderMethodHandleId getValue() {
        return value;
    }
}
