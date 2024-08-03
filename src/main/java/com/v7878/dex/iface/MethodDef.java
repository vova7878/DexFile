package com.v7878.dex.iface;

import java.util.List;

public non-sealed interface MethodDef extends MemberDef, Comparable<MethodDef> {
    TypeId getReturnType();

    List<? extends Parameter> getParameters();

    MethodImplementation getImplementation();
}
