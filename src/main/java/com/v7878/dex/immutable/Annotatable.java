package com.v7878.dex.immutable;

import com.v7878.dex.util.MemberUtils;

import java.util.NavigableSet;

public sealed interface Annotatable permits ClassDef, MemberDef, Parameter {
    NavigableSet<Annotation> getAnnotations();

    default Annotation findAnnotation(TypeId type) {
        return MemberUtils.findAnnotation(getAnnotations(), type);
    }
}
