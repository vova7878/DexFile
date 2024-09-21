package com.v7878.dex.immutable;

import java.util.Set;

public sealed interface Annotatable permits ClassDef, MemberDef, Parameter {
    Set<Annotation> getAnnotations();
}
