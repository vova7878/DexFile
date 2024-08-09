package com.v7878.dex.reader;

import com.v7878.dex.base.BaseAnnotationElement;
import com.v7878.dex.iface.value.EncodedValue;
import com.v7878.dex.io.RandomInput;
import com.v7878.dex.reader.value.EncodedValueReader;

public class ReaderAnnotationElement extends BaseAnnotationElement {
    private final String name;
    private final EncodedValue value;

    private ReaderAnnotationElement(String name, EncodedValue value) {
        this.name = name;
        this.value = value;
    }

    public static ReaderAnnotationElement readElement(ReaderDex dexfile, RandomInput in) {
        String name = dexfile.getString(in.readULeb128());
        EncodedValue value = EncodedValueReader.readValue(dexfile, in);
        return new ReaderAnnotationElement(name, value);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public EncodedValue getValue() {
        return value;
    }
}
