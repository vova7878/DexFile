package com.v7878.dex.immutable;

import com.v7878.dex.util.CollectionUtils;

import java.util.NavigableSet;
import java.util.Objects;

public sealed interface Annotatable permits ClassDef, MemberDef, Parameter {
    NavigableSet<Annotation> getAnnotations();

    default Annotation findAnnotation(TypeId type) {
        Objects.requireNonNull(type);
        return CollectionUtils.findValue(getAnnotations(), CollectionUtils.comparable(
                value -> CollectionUtils.compareNonNull(value.getType(), type)));
    }
}
