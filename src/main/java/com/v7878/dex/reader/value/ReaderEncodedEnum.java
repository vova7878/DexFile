package com.v7878.dex.reader.value;

import com.v7878.dex.base.value.BaseEncodedEnum;
import com.v7878.dex.io.RandomInput;
import com.v7878.dex.io.ValueCoder;
import com.v7878.dex.reader.ReaderDex;
import com.v7878.dex.reader.ReaderFieldId;

public class ReaderEncodedEnum extends BaseEncodedEnum {
    private final ReaderFieldId value;

    private ReaderEncodedEnum(ReaderFieldId value) {
        this.value = value;
    }

    public static ReaderEncodedEnum readValue(ReaderDex dexfile, RandomInput in, int arg) {
        return new ReaderEncodedEnum(dexfile.getFieldId(
                ValueCoder.readUnsignedInt(in, arg, false)));
    }

    @Override
    public ReaderFieldId getValue() {
        return value;
    }
}
