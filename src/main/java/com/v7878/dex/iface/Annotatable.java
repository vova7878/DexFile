package com.v7878.dex.iface;

import java.util.NavigableSet;

public interface Annotatable {
    NavigableSet<? extends Annotation> getAnnotations();
}
