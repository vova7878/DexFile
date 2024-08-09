package com.v7878.dex.iface;

import java.util.Set;

public interface Annotatable {
    Set<? extends Annotation> getAnnotations();
}
