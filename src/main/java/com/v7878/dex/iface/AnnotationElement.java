package com.v7878.dex.iface;

import com.v7878.dex.iface.value.EncodedValue;

public interface AnnotationElement extends Comparable<AnnotationElement> {
    String getName();

    EncodedValue getValue();
}
