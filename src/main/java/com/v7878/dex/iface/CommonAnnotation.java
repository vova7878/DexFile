package com.v7878.dex.iface;

import java.util.Set;

public interface CommonAnnotation {
    TypeId getType();

    Set<? extends AnnotationElement> getElements();
}
