package com.v7878.dex.iface;

import com.v7878.dex.AnnotationVisibility;

public interface Annotation extends CommonAnnotation, Comparable<Annotation> {
    AnnotationVisibility getVisibility();
}
