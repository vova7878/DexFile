package com.v7878.dex.immutable;

import java.util.Set;

public interface CommonAnnotation {
    TypeId getType();

    Set<AnnotationElement> getElements();
}
