package com.v7878.dex.reader;

import com.v7878.dex.base.BaseFieldId;

public class ReaderFieldId extends BaseFieldId implements ReaderMemberId {
    public static final int ITEM_SIZE = 8;

    public static final int DECLARING_CLASS_OFFSET = 0;
    public static final int TYPE_OFFSET = 2;
    public static final int NAME_OFFSET = 4;

    private final ReaderDex dexfile;
    private final int offset;

    public ReaderFieldId(ReaderDex dexfile, int index, int field_ids_off) {
        this.dexfile = dexfile;
        this.offset = field_ids_off + index * ITEM_SIZE;
    }

    private ReaderTypeId declaring_class;
    private String name;
    private ReaderTypeId type;

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

    @Override
    public ReaderTypeId getType() {
        if (type != null) return type;
        return type = dexfile.getTypeId(
                dexfile.mainAt(offset + TYPE_OFFSET).readUShort());
    }
}
