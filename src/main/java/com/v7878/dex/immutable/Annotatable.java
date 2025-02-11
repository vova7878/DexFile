package com.v7878.dex.immutable;

import java.util.NavigableSet;

public sealed interface Annotatable permits ClassDef, MemberDef, Parameter {
    NavigableSet<Annotation> getAnnotations();
}
