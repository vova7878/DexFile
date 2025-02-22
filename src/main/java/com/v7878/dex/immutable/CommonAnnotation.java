package com.v7878.dex.immutable;

import com.v7878.dex.immutable.value.EncodedAnnotation;
import com.v7878.dex.util.MemberUtils;

import java.util.NavigableSet;

public sealed interface CommonAnnotation permits Annotation, EncodedAnnotation {
    TypeId getType();

    NavigableSet<AnnotationElement> getElements();

    default AnnotationElement findElement(String name) {
        return MemberUtils.findElement(getElements(), name);
    }
}
