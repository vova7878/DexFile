package com.v7878.dex;

import com.v7878.dex.immutable.CallSiteId;
import com.v7878.dex.immutable.FieldId;
import com.v7878.dex.immutable.MethodHandleId;
import com.v7878.dex.immutable.MethodId;
import com.v7878.dex.immutable.ProtoId;
import com.v7878.dex.immutable.TypeId;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public enum ReferenceType {
    STRING {
        @Override
        public String validate(Object ref) {
            return (String) Objects.requireNonNull(ref);
        }

        @Override
        public String indexToRef(ReferenceIndexer indexer, int index) {
            return indexer.getString(index);
        }
    }, TYPE {
        @Override
        public TypeId validate(Object ref) {
            return (TypeId) Objects.requireNonNull(ref);
        }

        @Override
        public TypeId indexToRef(ReferenceIndexer indexer, int index) {
            return indexer.getTypeId(index);
        }
    }, FIELD {
        @Override
        public FieldId validate(Object ref) {
            return (FieldId) Objects.requireNonNull(ref);
        }

        @Override
        public FieldId indexToRef(ReferenceIndexer indexer, int index) {
            return indexer.getFieldId(index);
        }
    }, METHOD {
        @Override
        public MethodId validate(Object ref) {
            return (MethodId) Objects.requireNonNull(ref);
        }

        @Override
        public MethodId indexToRef(ReferenceIndexer indexer, int index) {
            return indexer.getMethodId(index);
        }
    }, PROTO {
        @Override
        public ProtoId validate(Object ref) {
            return (ProtoId) Objects.requireNonNull(ref);
        }

        @Override
        public ProtoId indexToRef(ReferenceIndexer indexer, int index) {
            return indexer.getProtoId(index);
        }
    }, CALLSITE {
        @Override
        public CallSiteId validate(Object ref) {
            return (CallSiteId) Objects.requireNonNull(ref);
        }

        @Override
        public CallSiteId indexToRef(ReferenceIndexer indexer, int index) {
            return indexer.getCallSiteId(index);
        }
    }, METHOD_HANDLE {
        @Override
        public MethodHandleId validate(Object ref) {
            return (MethodHandleId) Objects.requireNonNull(ref);
        }

        @Override
        public MethodHandleId indexToRef(ReferenceIndexer indexer, int index) {
            return indexer.getMethodHandleId(index);
        }
    }, RAW_INDEX {
        @Override
        public Integer validate(Object ref) {
            return Preconditions.checkRawIndex((Integer) Objects.requireNonNull(ref));
        }

        @Override
        public Integer indexToRef(ReferenceIndexer indexer, int index) {
            return index;
        }
    };

    public abstract Object validate(Object ref);

    public interface ReferenceIndexer {
        String getString(int index);

        TypeId getTypeId(int index);

        FieldId getFieldId(int index);

        ProtoId getProtoId(int index);

        MethodId getMethodId(int index);

        MethodHandleId getMethodHandleId(int index);

        CallSiteId getCallSiteId(int index);
    }

    public abstract Object indexToRef(ReferenceIndexer indexer, int index);

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
