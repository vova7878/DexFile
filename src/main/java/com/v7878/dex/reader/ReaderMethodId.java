package com.v7878.dex.reader;

import com.v7878.dex.base.BaseMethodId;

import java.util.List;

public class ReaderMethodId extends BaseMethodId implements ReaderMemberId {
    public static final int ITEM_SIZE = 8;

    public static final int DECLARING_CLASS_OFFSET = 0;
    public static final int PROTO_OFFSET = 2;
    public static final int NAME_OFFSET = 4;

    private final ReaderDex dexfile;
    private final int offset;

    public ReaderMethodId(ReaderDex dexfile, int index, int method_ids_off) {
        this.dexfile = dexfile;
        this.offset = method_ids_off + index * ITEM_SIZE;
    }

    private ReaderTypeId declaring_class;
    private String name;
    private ReaderProtoId proto;

    @Override
    public ReaderTypeId getDeclaringClass() {
        if (declaring_class != null) return declaring_class;
        return declaring_class = dexfile.getTypeId(
                dexfile.mainAt(offset + DECLARING_CLASS_OFFSET).readUShort());
    }

    @Override
    public String getName() {
        if (name != null) return name;
        return name = dexfile.getString(
                dexfile.mainAt(offset + NAME_OFFSET).readSmallUInt());
    }

    public ReaderProtoId getProto() {
        if (proto != null) return proto;
        return proto = dexfile.getProtoId(
                dexfile.mainAt(offset + PROTO_OFFSET).readUShort());
    }

    @Override
    public ReaderTypeId getReturnType() {
        return getProto().getReturnType();
    }

    @Override
    public List<ReaderTypeId> getParameterTypes() {
        return getProto().getParameterTypes();
    }
}
