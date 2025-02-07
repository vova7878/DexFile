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
        public String indexToRef(ReferenceStorage storage, int index) {
            return storage.getString(index);
        }

        @Override
        public void collect(ReferenceCollector colleactor, Object value) {
            colleactor.add(validate(value));
        }

        @Override
        public int refToIndex(ReferenceIndexer indexer, Object value) {
            return indexer.getStringIndex(validate(value));
        }
    }, TYPE {
        @Override
        public TypeId validate(Object ref) {
            return (TypeId) Objects.requireNonNull(ref);
        }

        @Override
        public TypeId indexToRef(ReferenceStorage storage, int index) {
            return storage.getTypeId(index);
        }

        @Override
        public void collect(ReferenceCollector colleactor, Object value) {
            colleactor.add(validate(value));
        }

        @Override
        public int refToIndex(ReferenceIndexer indexer, Object value) {
            return indexer.getTypeIndex(validate(value));
        }
    }, FIELD {
        @Override
        public FieldId validate(Object ref) {
            return (FieldId) Objects.requireNonNull(ref);
        }

        @Override
        public FieldId indexToRef(ReferenceStorage storage, int index) {
            return storage.getFieldId(index);
        }

        @Override
        public void collect(ReferenceCollector colleactor, Object value) {
            colleactor.add(validate(value));
        }

        @Override
        public int refToIndex(ReferenceIndexer indexer, Object value) {
            return indexer.getFieldIndex(validate(value));
        }
    }, METHOD {
        @Override
        public MethodId validate(Object ref) {
            return (MethodId) Objects.requireNonNull(ref);
        }

        @Override
        public MethodId indexToRef(ReferenceStorage storage, int index) {
            return storage.getMethodId(index);
        }

        @Override
        public void collect(ReferenceCollector colleactor, Object value) {
            colleactor.add(validate(value));
        }

        @Override
        public int refToIndex(ReferenceIndexer indexer, Object value) {
            return indexer.getMethodIndex(validate(value));
        }
    }, PROTO {
        @Override
        public ProtoId validate(Object ref) {
            return (ProtoId) Objects.requireNonNull(ref);
        }

        @Override
        public ProtoId indexToRef(ReferenceStorage storage, int index) {
            return storage.getProtoId(index);
        }

        @Override
        public void collect(ReferenceCollector colleactor, Object value) {
            colleactor.add(validate(value));
        }

        @Override
        public int refToIndex(ReferenceIndexer indexer, Object value) {
            return indexer.getProtoIndex(validate(value));
        }
    }, CALLSITE {
        @Override
        public CallSiteId validate(Object ref) {
            return (CallSiteId) Objects.requireNonNull(ref);
        }

        @Override
        public CallSiteId indexToRef(ReferenceStorage storage, int index) {
            return storage.getCallSiteId(index);
        }

        @Override
        public void collect(ReferenceCollector colleactor, Object value) {
            colleactor.add(validate(value));
        }

        @Override
        public int refToIndex(ReferenceIndexer indexer, Object value) {
            return indexer.getCallSiteIndex(validate(value));
        }
    }, METHOD_HANDLE {
        @Override
        public MethodHandleId validate(Object ref) {
            return (MethodHandleId) Objects.requireNonNull(ref);
        }

        @Override
        public MethodHandleId indexToRef(ReferenceStorage storage, int index) {
            return storage.getMethodHandleId(index);
        }

        @Override
        public void collect(ReferenceCollector colleactor, Object value) {
            colleactor.add(validate(value));
        }

        @Override
        public int refToIndex(ReferenceIndexer indexer, Object value) {
            return indexer.getMethodHandleIndex(validate(value));
        }
    }, RAW_INDEX {
        @Override
        public Integer validate(Object ref) {
            return Preconditions.checkRawIndex((Integer) Objects.requireNonNull(ref));
        }

        @Override
        public Integer indexToRef(ReferenceStorage storage, int index) {
            return index;
        }

        @Override
        public void collect(ReferenceCollector colleactor, Object value) {
            // nop
        }

        @Override
        public int refToIndex(ReferenceIndexer indexer, Object value) {
            return validate(value);
        }
    };

    public abstract Object validate(Object ref);

    public interface ReferenceStorage {
        String getString(int index);

        TypeId getTypeId(int index);

        FieldId getFieldId(int index);

        ProtoId getProtoId(int index);

        MethodId getMethodId(int index);

        MethodHandleId getMethodHandleId(int index);

        CallSiteId getCallSiteId(int index);
    }

    public abstract Object indexToRef(ReferenceStorage storage, int index);

    public interface ReferenceCollector {
        void add(String value);

        void add(TypeId value);

        void add(FieldId value);

        void add(ProtoId value);

        void add(MethodId value);

        void add(MethodHandleId value);

        void add(CallSiteId value);
    }

    public abstract void collect(ReferenceCollector colleactor, Object value);

    public interface ReferenceIndexer {
        int getStringIndex(String value);

        int getTypeIndex(TypeId value);

        int getFieldIndex(FieldId value);

        int getProtoIndex(ProtoId value);

        int getMethodIndex(MethodId value);

        int getMethodHandleIndex(MethodHandleId value);

        int getCallSiteIndex(CallSiteId value);
    }

    public abstract int refToIndex(ReferenceIndexer indexer, Object value);
}
