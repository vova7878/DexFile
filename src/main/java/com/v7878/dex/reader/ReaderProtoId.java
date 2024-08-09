package com.v7878.dex.reader;

import com.v7878.dex.base.BaseProtoId;
import com.v7878.dex.iface.TypeId;
import com.v7878.dex.reader.raw.TypeList;

public class ReaderProtoId extends BaseProtoId {
    public static final int ITEM_SIZE = 12;

    public static final int SHORTY_OFFSET = 0;
    public static final int RETURN_TYPE_OFFSET = 4;
    public static final int PARAMETERS_OFFSET = 8;

    private final ReaderDex dexfile;
    private final int offset;

    public ReaderProtoId(ReaderDex dexfile, int index, int proto_ids_off) {
        this.dexfile = dexfile;
        this.offset = proto_ids_off + index * ITEM_SIZE;
    }

    private ReaderTypeId return_type;
    private TypeList parameters;

    @Override
    public TypeId getReturnType() {
        if (return_type != null) return return_type;
        return return_type = dexfile.getTypeId(
                dexfile.mainAt(offset + RETURN_TYPE_OFFSET).readSmallUInt());
    }

    @Override
    public TypeList getParameterTypes() {
        if (parameters != null) return parameters;
        return parameters = dexfile.getTypeList(
                dexfile.mainAt(offset + PARAMETERS_OFFSET).readSmallUInt());
    }
}
