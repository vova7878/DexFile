package com.v7878.dex.reader;

import com.v7878.dex.ValueType;
import com.v7878.dex.base.BaseCallSiteId;
import com.v7878.dex.iface.ProtoId;
import com.v7878.dex.iface.value.EncodedValue;
import com.v7878.dex.reader.value.ReaderEncodedArray;
import com.v7878.dex.reader.value.ReaderEncodedMethodHandle;
import com.v7878.dex.reader.value.ReaderEncodedMethodType;
import com.v7878.dex.reader.value.ReaderEncodedString;

import java.util.List;

public class ReaderCallSiteId extends BaseCallSiteId {
    public static final int ITEM_SIZE = 4;

    private final ReaderDex dexfile;
    private final int index;
    private final int offset;

    public ReaderCallSiteId(ReaderDex dexfile, int index, int callsite_ids_off) {
        this.dexfile = dexfile;
        this.index = index;
        this.offset = callsite_ids_off + index * ITEM_SIZE;
    }

    private ReaderEncodedArray array;
    private ReaderMethodHandleId handle;
    private String name;
    private ReaderProtoId proto;
    private List<? extends EncodedValue> extra_args;

    public ReaderEncodedArray getOriginalArray() {
        if (array != null) return array;
        return array = dexfile.getEncodedArray(dexfile.dataAt(offset).readSmallUInt());
    }

    @Override
    public ReaderMethodHandleId getMethodHandle() {
        if (handle != null) return handle;
        EncodedValue value = getOriginalArray().getValue().get(0);
        if (value.getValueType() != ValueType.METHOD_HANDLE) {
            throw new IllegalStateException(String.format(
                    "Invalid encoded value type (%s) for the first item in call site %d",
                    value.getValueType(), index));
        }
        return handle = ((ReaderEncodedMethodHandle) value).getValue();
    }

    @Override
    public String getMethodName() {
        if (name != null) return name;
        EncodedValue value = getOriginalArray().getValue().get(1);
        if (value.getValueType() != ValueType.STRING) {
            throw new IllegalStateException(String.format(
                    "Invalid encoded value type (%s) for the second item in call site %d",
                    value.getValueType(), index));
        }
        return name = ((ReaderEncodedString) value).getValue();
    }

    @Override
    public ProtoId getMethodProto() {
        if (proto != null) return proto;
        EncodedValue value = getOriginalArray().getValue().get(2);
        if (value.getValueType() != ValueType.METHOD_TYPE) {
            throw new IllegalStateException(String.format(
                    "Invalid encoded value type (%s) for the third item in call site %d",
                    value.getValueType(), index));
        }
        return proto = ((ReaderEncodedMethodType) value).getValue();
    }

    @Override
    public List<? extends EncodedValue> getExtraArguments() {
        if (extra_args != null) return extra_args;
        var all_args = getOriginalArray().getValue();
        if (all_args.size() <= 3) {
            return extra_args = List.of();
        }
        return extra_args = all_args.subList(3, all_args.size());
    }
}
