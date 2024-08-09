package com.v7878.dex.reader;

import com.v7878.dex.MethodHandleType;
import com.v7878.dex.base.BaseMethodHandleId;

public class ReaderMethodHandleId extends BaseMethodHandleId {
    public static final int ITEM_SIZE = 8;

    public static final int METHOD_HANDLE_TYPE_OFFSET = 0;
    public static final int MEMBER_ID_OFFSET = 4;

    private final ReaderDex dexfile;
    private final int offset;

    public ReaderMethodHandleId(ReaderDex dexfile, int index, int method_handles_off) {
        this.dexfile = dexfile;
        this.offset = method_handles_off + index * ITEM_SIZE;
    }

    private MethodHandleType type;
    private ReaderMemberId member;

    @Override
    public MethodHandleType getHandleType() {
        if (type != null) return type;
        return type = MethodHandleType.of(dexfile.mainAt(
                offset + METHOD_HANDLE_TYPE_OFFSET).readUShort());
    }

    @Override
    public ReaderMemberId getMember() {
        if (member != null) return member;
        int member_index = dexfile.mainAt(offset + MEMBER_ID_OFFSET).readUShort();
        return member = getHandleType().isMethodAccess() ?
                dexfile.getMethodId(member_index) : dexfile.getFieldId(member_index);
    }
}
