package com.v7878.dex;

import com.v7878.dex.bytecode.Instruction;

public interface DataCollector {

    void add(String value);

    void add(TypeId value);

    void add(ProtoId value);

    void add(FieldId value);

    void add(MethodId value);

    void add(MethodHandleItem value);

    void add(CallSiteId value);

    void add(ClassDef value);

    void add(ClassData value);

    void add(TypeList value);

    void add(AnnotationItem value);

    void add(AnnotationSet value);

    void add(AnnotationSetList value);

    void add(ClassDef clazz, AnnotationsDirectory value);

    void add(EncodedValue.ArrayValue value);

    void add(CodeItem value);

    void fill(CatchHandlerElement value);

    void fill(CatchHandler value);

    void fill(Instruction value);

    void fill(TryItem value);

    void fill(EncodedField value);

    void fill(EncodedMethod value);

    void fill(EncodedAnnotation value);

    void fill(AnnotationElement value);

    void fill(EncodedValue value);
}
