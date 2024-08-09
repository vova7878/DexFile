package com.v7878.dex.reader.value;

import com.v7878.dex.base.value.BaseEncodedField;
import com.v7878.dex.io.RandomInput;
import com.v7878.dex.io.ValueCoder;
import com.v7878.dex.reader.ReaderDex;
import com.v7878.dex.reader.ReaderFieldId;

public class ReaderEncodedField extends BaseEncodedField {
    private final ReaderFieldId value;

    private ReaderEncodedField(ReaderFieldId value) {
        this.value = value;
    }

    public static ReaderEncodedField readValue(ReaderDex dexfile, RandomInput in, int arg) {
        return new ReaderEncodedField(dexfile.getFieldId(
                ValueCoder.readUnsignedInt(in, arg, false)));
    }

    @Override
    public ReaderFieldId getValue() {
        return value;
    }
}
