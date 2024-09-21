package com.v7878.dex.immutable;

import com.v7878.dex.immutable.value.EncodedAnnotation;

import java.util.Set;

public sealed interface CommonAnnotation permits Annotation, EncodedAnnotation {
    TypeId getType();

    Set<AnnotationElement> getElements();
}
