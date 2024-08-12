package com.v7878.dex.reader;

import static com.v7878.dex.DexConstants.NO_OFFSET;

import com.v7878.dex.base.BaseProtoId;
import com.v7878.dex.reader.util.OptionalUtils;

import java.util.List;

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
    private List<ReaderTypeId> parameters;

    @Override
    public ReaderTypeId getReturnType() {
        if (return_type != null) return return_type;
        return return_type = dexfile.getTypeId(
                dexfile.mainAt(offset + RETURN_TYPE_OFFSET).readSmallUInt());
    }

    @Override
    public List<ReaderTypeId> getParameterTypes() {
        if (parameters != null) return parameters;
        return parameters = OptionalUtils.getOrDefault(
                dexfile.mainAt(offset + PARAMETERS_OFFSET).readSmallUInt(),
                NO_OFFSET, dexfile::getTypeList, List.of());
    }
}
