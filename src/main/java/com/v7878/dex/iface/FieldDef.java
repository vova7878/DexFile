package com.v7878.dex.iface;

import com.v7878.dex.iface.value.EncodedValue;

public non-sealed interface FieldDef extends MemberDef, Comparable<FieldDef> {
    TypeId getType();

    EncodedValue getInitialValue();
}
