package com.v7878.dex.util;

import com.v7878.dex.DexIO.DexReaderCache;
import com.v7878.dex.immutable.Dex;
import com.v7878.dex.immutable.FieldId;
import com.v7878.dex.immutable.MethodHandleId;
import com.v7878.dex.immutable.MethodId;
import com.v7878.dex.immutable.ProtoId;
import com.v7878.dex.immutable.TypeId;
import com.v7878.dex.raw.DexCollector.CallSiteIdContainer;
import com.v7878.dex.raw.DexCollector.ClassDefContainer;
import com.v7878.dex.raw.SharedData.StringPosition;

public class EmptyArrays {
    public static final int[] INT = new int[0];
    public static final Object[] OBJECT = new Object[0];
    public static final StringPosition[] STRING = new StringPosition[0];

    public static final TypeId[] TYPE_ID = new TypeId[0];
    public static final ProtoId[] PROTO_ID = new ProtoId[0];
    public static final FieldId[] FIELD_ID = new FieldId[0];
    public static final MethodId[] METHOD_ID = new MethodId[0];
    public static final CallSiteIdContainer[] CALLSITE_ID_CONTAINER = new CallSiteIdContainer[0];
    public static final MethodHandleId[] METHOD_HANDLE_ID = new MethodHandleId[0];
    public static final ClassDefContainer[] CLASS_DEF_CONTAINER = new ClassDefContainer[0];

    public static final Dex[] DEX = new Dex[0];
    public static final DexReaderCache[] DEX_READER_CACHE = new DexReaderCache[0];
}
