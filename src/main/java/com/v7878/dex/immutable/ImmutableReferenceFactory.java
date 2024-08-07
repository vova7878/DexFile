package com.v7878.dex.immutable;

import com.v7878.dex.ReferenceType;
import com.v7878.dex.iface.CallSiteId;
import com.v7878.dex.iface.FieldId;
import com.v7878.dex.iface.MethodHandleId;
import com.v7878.dex.iface.MethodId;
import com.v7878.dex.iface.ProtoId;
import com.v7878.dex.iface.TypeId;

public final class ImmutableReferenceFactory {
    public static Object of(Object other) {
        return of(ReferenceType.of(other), other);
    }

    public static Object of(ReferenceType type, Object other) {
        return switch (type) {
            case STRING -> (String) type.validate(other);
            case RAW_INDEX -> (Integer) type.validate(other);
            case TYPE -> ImmutableTypeId.of((TypeId) type.validate(other));
            case FIELD -> ImmutableFieldId.of((FieldId) type.validate(other));
            case METHOD -> ImmutableMethodId.of((MethodId) type.validate(other));
            case PROTO -> ImmutableProtoId.of((ProtoId) type.validate(other));
            case METHOD_HANDLE -> ImmutableMethodHandleId.of((MethodHandleId) type.validate(other));
            case CALLSITE -> ImmutableCallSiteId.of((CallSiteId) type.validate(other));
        };
    }
}
