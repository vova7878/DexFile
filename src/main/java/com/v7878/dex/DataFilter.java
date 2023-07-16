package com.v7878.dex;

import com.v7878.dex.bytecode.Instruction;

public class DataFilter implements DataCollector {

    @Override
    public void add(String value) {
    }

    @Override
    public void add(TypeId value) {
        value.collectData(this);
    }

    @Override
    public void add(ProtoId value) {
        value.collectData(this);
    }

    @Override
    public void add(FieldId value) {
        value.collectData(this);
    }

    @Override
    public void add(MethodId value) {
        value.collectData(this);
    }

    @Override
    public void add(MethodHandleItem value) {
        value.collectData(this);
    }

    @Override
    public void add(CallSiteId value) {
        value.collectData(this);
    }

    @Override
    public void add(ClassDef value) {
        value.collectData(this);
    }

    @Override
    public void add(ClassData value) {
        value.collectData(this);
    }

    @Override
    public void add(TypeList value) {
        value.collectData(this);
    }

    @Override
    public void add(AnnotationItem value) {
        value.collectData(this);
    }

    @Override
    public void add(AnnotationSet value) {
        value.collectData(this);
    }

    @Override
    public void add(AnnotationSetList value) {
        value.collectData(this);
    }

    @Override
    public void add(ClassDef clazz, AnnotationsDirectory value) {
    }

    @Override
    public void add(EncodedValue.ArrayValue value) {
        value.collectData(this);
    }

    @Override
    public void add(CodeItem value) {
        value.collectData(this);
    }

    @Override
    public void fill(CatchHandlerElement value) {
        value.collectData(this);
    }

    @Override
    public void fill(CatchHandler value) {
        value.collectData(this);
    }

    @Override
    public void fill(Instruction value) {
        value.collectData(this);
    }

    @Override
    public void fill(TryItem value) {
        value.collectData(this);
    }

    @Override
    public void fill(EncodedField value) {
        value.collectData(this);
    }

    @Override
    public void fill(EncodedMethod value) {
        value.collectData(this);
    }

    @Override
    public void fill(EncodedAnnotation value) {
        value.collectData(this);
    }

    @Override
    public void fill(AnnotationElement value) {
        value.collectData(this);
    }

    @Override
    public void fill(EncodedValue value) {
        value.collectData(this);
    }
}
