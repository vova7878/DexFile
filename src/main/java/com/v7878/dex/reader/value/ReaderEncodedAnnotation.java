package com.v7878.dex.reader.value;

import com.v7878.dex.base.value.BaseEncodedAnnotation;
import com.v7878.dex.io.RandomInput;
import com.v7878.dex.reader.ReaderAnnotationElement;
import com.v7878.dex.reader.ReaderDex;
import com.v7878.dex.reader.ReaderTypeId;
import com.v7878.dex.reader.util.CachedVariableSizeSet;

import java.util.Set;

public class ReaderEncodedAnnotation extends BaseEncodedAnnotation {
    private final ReaderTypeId type;
    private final Set<? extends ReaderAnnotationElement> elements;

    private ReaderEncodedAnnotation(
            ReaderTypeId type, Set<? extends ReaderAnnotationElement> elements) {
        this.type = type;
        this.elements = elements;
    }

    private static ReaderEncodedAnnotation readValue(
            ReaderDex dexfile, RandomInput in, boolean lazy) {
        ReaderTypeId type = dexfile.getTypeId(in.readULeb128());
        Set<ReaderAnnotationElement> elements;
        int size = in.readULeb128();
        if (size == 0) {
            elements = Set.of();
        } else {
            var tmp = new CachedVariableSizeSet<ReaderAnnotationElement>(size) {
                @Override
                protected ReaderAnnotationElement computeNext() {
                    // TODO: what if exception?
                    return ReaderAnnotationElement.readElement(dexfile, in);
                }
            };
            if (!lazy) tmp.computeAll();
            elements = tmp;
        }
        return new ReaderEncodedAnnotation(type, elements);
    }

    public static ReaderEncodedAnnotation readValue(ReaderDex dexfile, RandomInput in) {
        return readValue(dexfile, in, false);
    }

    public static ReaderEncodedAnnotation readValue(ReaderDex dexfile, int offset) {
        return readValue(dexfile, dexfile.dataAt(offset), true);
    }

    @Override
    public ReaderTypeId getType() {
        return type;
    }

    @Override
    public Set<? extends ReaderAnnotationElement> getElements() {
        return elements;
    }
}
