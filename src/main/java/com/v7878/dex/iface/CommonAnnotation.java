package com.v7878.dex.iface;

import java.util.NavigableSet;

public interface CommonAnnotation {
    TypeId getType();

    NavigableSet<? extends AnnotationElement> getElements();
}
