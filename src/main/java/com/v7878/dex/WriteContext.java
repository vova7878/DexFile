package com.v7878.dex;

public interface WriteContext {
    DexOptions getOptions();

    int getStringIndex(String value);

    int getTypeIndex(TypeId value);

    int getProtoIndex(ProtoId value);

    int getFieldIndex(FieldId value);

    int getMethodIndex(MethodId value);

    int getCallSiteIndex(CallSiteId value);

    int getMethodHandleIndex(MethodHandleItem value);

    int getTypeListOffset(TypeList value);

    int getAnnotationOffset(AnnotationItem value);

    int getAnnotationSetOffset(AnnotationSet value);

    int getAnnotationSetListOffset(AnnotationSetList value);

    int getClassDataOffset(ClassData value);

    int getAnnotationsDirectoryOffset(ClassDef value);

    int getArrayValueOffset(EncodedValue.ArrayValue value);

    int getCodeItemOffset(CodeItem value);
}
