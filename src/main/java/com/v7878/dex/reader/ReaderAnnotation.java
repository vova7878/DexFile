package com.v7878.dex.reader;

import com.v7878.dex.AnnotationVisibility;
import com.v7878.dex.base.BaseAnnotation;
import com.v7878.dex.iface.TypeId;
import com.v7878.dex.reader.value.ReaderEncodedAnnotation;

import java.util.Set;

public class ReaderAnnotation extends BaseAnnotation {
    public static final int VISIBILITY_OFFSET = 0;
    public static final int ANNOTATION_OFFSET = 1;

    private final ReaderDex dexfile;
    private final int offset;

    public ReaderAnnotation(ReaderDex dexfile, int offset) {
        this.dexfile = dexfile;
        this.offset = offset;
    }

    private AnnotationVisibility visibility;
    private ReaderEncodedAnnotation encoded_annotation;

    @Override
    public AnnotationVisibility getVisibility() {
        if (visibility != null) return visibility;
        return visibility = AnnotationVisibility.of(dexfile.dataAt(
                offset + VISIBILITY_OFFSET).readUByte());
    }

    public ReaderEncodedAnnotation getEncodedAnnotation() {
        if (encoded_annotation != null) return encoded_annotation;
        return encoded_annotation = ReaderEncodedAnnotation
                .readValue(dexfile, offset + ANNOTATION_OFFSET);
    }

    @Override
    public TypeId getType() {
        return getEncodedAnnotation().getType();
    }

    @Override
    public Set<? extends ReaderAnnotationElement> getElements() {
        return getEncodedAnnotation().getElements();
    }
}
