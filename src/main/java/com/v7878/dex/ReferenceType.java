package com.v7878.dex;

import com.v7878.dex.iface.CallSiteId;
import com.v7878.dex.iface.FieldId;
import com.v7878.dex.iface.MethodHandleId;
import com.v7878.dex.iface.MethodId;
import com.v7878.dex.iface.ProtoId;
import com.v7878.dex.iface.TypeId;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public enum ReferenceType {
    STRING {
        @Override
        public String validate(Object ref) {
            return (String) Objects.requireNonNull(ref);
        }
    }, TYPE {
        @Override
        public TypeId validate(Object ref) {
            return (TypeId) Objects.requireNonNull(ref);
        }
    }, FIELD {
        @Override
        public FieldId validate(Object ref) {
            return (FieldId) Objects.requireNonNull(ref);
        }
    }, METHOD {
        @Override
        public MethodId validate(Object ref) {
            return (MethodId) Objects.requireNonNull(ref);
        }
    }, PROTO {
        @Override
        public ProtoId validate(Object ref) {
            return (ProtoId) Objects.requireNonNull(ref);
        }
    }, CALLSITE {
        @Override
        public CallSiteId validate(Object ref) {
            return (CallSiteId) Objects.requireNonNull(ref);
        }
    }, METHOD_HANDLE {
        @Override
        public MethodHandleId validate(Object ref) {
            return (MethodHandleId) Objects.requireNonNull(ref);
        }
    }, RAW_INDEX {
        @Override
        public Integer validate(Object ref) {
            return Preconditions.checkRawIndex((Integer) Objects.requireNonNull(ref));
        }
    };

    public abstract Object validate(Object ref);

    public static ReferenceType of(Object reference) {
        if (reference instanceof String) return STRING;
        if (reference instanceof Integer) return RAW_INDEX;
        if (reference instanceof TypeId) return TYPE;
        if (reference instanceof FieldId) return FIELD;
        if (reference instanceof MethodId) return METHOD;
        if (reference instanceof ProtoId) return PROTO;
        if (reference instanceof MethodHandleId) return METHOD_HANDLE;
        if (reference instanceof CallSiteId) return CALLSITE;
        throw new IllegalArgumentException("Invalid reference type");
    }
}
