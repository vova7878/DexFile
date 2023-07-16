package com.v7878.dex.bytecode;

import com.v7878.dex.CallSiteId;
import com.v7878.dex.DataCollector;
import com.v7878.dex.FieldId;
import com.v7878.dex.MethodHandleItem;
import com.v7878.dex.MethodId;
import com.v7878.dex.ProtoId;
import com.v7878.dex.ReadContext;
import com.v7878.dex.TypeId;
import com.v7878.dex.WriteContext;

import java.util.Objects;

public enum ReferenceType {
    STRING {
        @Override
        public String verify(Object ref) {
            return (String) Objects.requireNonNull(ref);
        }

        @Override
        public void collectData(DataCollector data, Object ref) {
            data.add(verify(ref));
        }

        @Override
        public String indexToRef(ReadContext context, int index) {
            return context.string(index);
        }

        @Override
        public int refToIndex(WriteContext context, Object ref) {
            return context.getStringIndex(verify(ref));
        }

        @Override
        public String clone(Object ref) {
            //no need to clone the string
            return verify(ref);
        }
    }, TYPE {
        @Override
        public TypeId verify(Object ref) {
            return (TypeId) Objects.requireNonNull(ref);
        }

        @Override
        public void collectData(DataCollector data, Object ref) {
            data.add(verify(ref));
        }

        @Override
        public TypeId indexToRef(ReadContext context, int index) {
            return context.type(index);
        }

        @Override
        public int refToIndex(WriteContext context, Object ref) {
            return context.getTypeIndex(verify(ref));
        }

        @Override
        public TypeId clone(Object ref) {
            return verify(ref).clone();
        }
    }, FIELD {
        @Override
        public FieldId verify(Object ref) {
            return (FieldId) Objects.requireNonNull(ref);
        }

        @Override
        public void collectData(DataCollector data, Object ref) {
            data.add(verify(ref));
        }

        @Override
        public FieldId indexToRef(ReadContext context, int index) {
            return context.field(index);
        }

        @Override
        public int refToIndex(WriteContext context, Object ref) {
            return context.getFieldIndex(verify(ref));
        }

        @Override
        public FieldId clone(Object ref) {
            return verify(ref).clone();
        }
    }, METHOD {
        @Override
        public MethodId verify(Object ref) {
            return (MethodId) Objects.requireNonNull(ref);
        }

        @Override
        public void collectData(DataCollector data, Object ref) {
            data.add(verify(ref));
        }

        @Override
        public MethodId indexToRef(ReadContext context, int index) {
            return context.method(index);
        }

        @Override
        public int refToIndex(WriteContext context, Object ref) {
            return context.getMethodIndex(verify(ref));
        }

        @Override
        public MethodId clone(Object ref) {
            return verify(ref).clone();
        }
    }, PROTO {
        @Override
        public ProtoId verify(Object ref) {
            return (ProtoId) Objects.requireNonNull(ref);
        }

        @Override
        public void collectData(DataCollector data, Object ref) {
            data.add(verify(ref));
        }

        @Override
        public ProtoId indexToRef(ReadContext context, int index) {
            return context.proto(index);
        }

        @Override
        public int refToIndex(WriteContext context, Object ref) {
            return context.getProtoIndex(verify(ref));
        }

        @Override
        public ProtoId clone(Object ref) {
            return verify(ref).clone();
        }
    }, CALLSITE {
        @Override
        public CallSiteId verify(Object ref) {
            return (CallSiteId) Objects.requireNonNull(ref);
        }

        @Override
        public void collectData(DataCollector data, Object ref) {
            data.add(verify(ref));
        }

        @Override
        public CallSiteId indexToRef(ReadContext context, int index) {
            return context.call_site(index);
        }

        @Override
        public int refToIndex(WriteContext context, Object ref) {
            return context.getCallSiteIndex(verify(ref));
        }

        @Override
        public CallSiteId clone(Object ref) {
            return verify(ref).clone();
        }
    }, METHOD_HANDLE {
        @Override
        public MethodHandleItem verify(Object ref) {
            return (MethodHandleItem) Objects.requireNonNull(ref);
        }

        @Override
        public void collectData(DataCollector data, Object ref) {
            data.add(verify(ref));
        }

        @Override
        public MethodHandleItem indexToRef(ReadContext context, int index) {
            return context.method_handle(index);
        }

        @Override
        public int refToIndex(WriteContext context, Object ref) {
            return context.getMethodHandleIndex(verify(ref));
        }

        @Override
        public MethodHandleItem clone(Object ref) {
            return verify(ref).clone();
        }
    }, RAW {
        @Override
        public Integer verify(Object ref) {
            return (Integer) Objects.requireNonNull(ref);
        }

        @Override
        public void collectData(DataCollector data, Object ref) {
            // nothing to do
        }

        @Override
        public Integer indexToRef(ReadContext context, int index) {
            return index;
        }

        @Override
        public int refToIndex(WriteContext context, Object ref) {
            return verify(ref);
        }

        @Override
        public Integer clone(Object ref) {
            return verify(ref);
        }
    };

    public abstract Object verify(Object ref);

    public abstract void collectData(DataCollector data, Object ref);

    public abstract Object indexToRef(ReadContext context, int index);

    public abstract int refToIndex(WriteContext context, Object ref);

    public abstract Object clone(Object ref);
}
