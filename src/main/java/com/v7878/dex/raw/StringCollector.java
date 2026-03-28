package com.v7878.dex.raw;

import com.v7878.dex.ReferenceType;
import com.v7878.dex.immutable.Annotation;
import com.v7878.dex.immutable.AnnotationElement;
import com.v7878.dex.immutable.CallSiteId;
import com.v7878.dex.immutable.ClassDef;
import com.v7878.dex.immutable.CommonAnnotation;
import com.v7878.dex.immutable.Dex;
import com.v7878.dex.immutable.ExceptionHandler;
import com.v7878.dex.immutable.FieldDef;
import com.v7878.dex.immutable.FieldId;
import com.v7878.dex.immutable.MethodDef;
import com.v7878.dex.immutable.MethodHandleId;
import com.v7878.dex.immutable.MethodId;
import com.v7878.dex.immutable.MethodImplementation;
import com.v7878.dex.immutable.ProtoId;
import com.v7878.dex.immutable.TryBlock;
import com.v7878.dex.immutable.TypeId;
import com.v7878.dex.immutable.bytecode.Instruction;
import com.v7878.dex.immutable.bytecode.iface.DualReferenceInstruction;
import com.v7878.dex.immutable.bytecode.iface.SingleReferenceInstruction;
import com.v7878.dex.immutable.debug.DebugItem;
import com.v7878.dex.immutable.debug.SetFile;
import com.v7878.dex.immutable.debug.StartLocal;
import com.v7878.dex.immutable.value.EncodedAnnotation;
import com.v7878.dex.immutable.value.EncodedArray;
import com.v7878.dex.immutable.value.EncodedEnum;
import com.v7878.dex.immutable.value.EncodedField;
import com.v7878.dex.immutable.value.EncodedMethod;
import com.v7878.dex.immutable.value.EncodedMethodHandle;
import com.v7878.dex.immutable.value.EncodedMethodType;
import com.v7878.dex.immutable.value.EncodedString;
import com.v7878.dex.immutable.value.EncodedType;
import com.v7878.dex.immutable.value.EncodedValue;
import com.v7878.dex.raw.SharedData.StringPosition;

import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.TreeSet;

public class StringCollector {
    public final NavigableSet<StringPosition> strings;
    private final SharedData shared_data;
    private final boolean debug;

    public StringCollector(SharedData shared_data, boolean debug) {
        this.shared_data = shared_data;
        this.debug = debug;

        strings = new TreeSet<>();
    }

    public void addString(String value) {
        strings.add(shared_data.addString(value));
    }

    public void fillType(TypeId value) {
        addString(value.getDescriptor());
    }

    public void fillField(FieldId value) {
        fillType(value.getDeclaringClass());
        addString(value.getName());
        fillType(value.getType());
    }

    public void fillProto(ProtoId value) {
        addString(value.computeShorty());
        fillType(value.getReturnType());
        fillTypeList(value.getParameterTypes());
    }

    public void fillMethod(MethodId value) {
        fillType(value.getDeclaringClass());
        addString(value.getName());
        fillProto(value.getProto());
    }

    public void fillMethodHandle(MethodHandleId value) {
        var member = value.getMember();
        if (value.getHandleType().isMethodAccess()) {
            fillMethod((MethodId) member);
        } else {
            fillField((FieldId) member);
        }
    }

    public void fillCallSite(CallSiteId value) {
        fillMethodHandle(value.getMethodHandle());
        addString(value.getMethodName());
        fillProto(value.getMethodProto());
        fillEncodedArray(EncodedArray.raw(value.getExtraArguments()));
    }

    public void fillClassDef(ClassDef value) {
        fillType(value.getType());
        var superclass = value.getSuperclass();
        if (superclass != null) fillType(superclass);
        fillTypeList(value.getInterfaces());
        var source_file = value.getSourceFile();
        if (source_file != null) addString(source_file);
        for (var field : value.getFields()) {
            fillFieldDef(field);
        }
        for (var method : value.getMethods()) {
            fillMethodDef(method);
        }
        fillAnnotationSet(value.getAnnotations());
    }


    public void fillAnnotationSet(NavigableSet<Annotation> value) {
        for (var tmp : value) {
            fillCommonAnnotation(tmp);
        }
    }

    public void fillTypeList(List<TypeId> value) {
        for (var tmp : value) {
            fillType(tmp);
        }
    }

    public void fillDebugInfo(List<DebugItem> value) {
        for (var tmp : value) {
            fillDebugItem(tmp);
        }
    }

    public void fillMethodImplementation(MethodImplementation value) {
        for (var tmp : value.getInstructions()) {
            fillInstruction(tmp);
        }
        for (var tmp : value.getTryBlocks()) {
            fillTryBlock(tmp);
        }
        if (debug) {
            fillDebugInfo(value.getDebugItems());
        }
    }

    public void fillDex(Dex value) {
        for (var tmp : value.getClasses()) {
            fillClassDef(tmp);
        }
    }

    public void collect(ReferenceType type, Object value) {
        Objects.requireNonNull(value);
        switch (type) {
            case STRING -> addString((String) value);
            case TYPE -> fillType((TypeId) value);
            case FIELD -> fillField((FieldId) value);
            case METHOD -> fillMethod((MethodId) value);
            case PROTO -> fillProto((ProtoId) value);
            case CALLSITE -> fillCallSite((CallSiteId) value);
            case METHOD_HANDLE -> fillMethodHandle((MethodHandleId) value);
            case RAW_INDEX -> { /* nop */ }
        }
    }

    public void fillInstruction(Instruction value) {
        if (value instanceof SingleReferenceInstruction ref1) {
            collect(ref1.getReferenceType1(), ref1.getReference1());
        }
        if (value instanceof DualReferenceInstruction ref2) {
            collect(ref2.getReferenceType2(), ref2.getReference2());
        }
    }

    public void fillExceptionHandler(ExceptionHandler value) {
        fillType(value.getExceptionType());
    }

    public void fillTryBlock(TryBlock value) {
        for (var tmp : value.getHandlers()) {
            fillExceptionHandler(tmp);
        }
    }

    public void fillFieldDef(FieldDef value) {
        addString(value.getName());
        fillType(value.getType());
        var initial_value = value.getInitialValue();
        if (initial_value != null) fillEncodedValue(value.getInitialValue());
        fillAnnotationSet(value.getAnnotations());
    }

    public void fillDebugItem(DebugItem value) {
        assert debug;
        if (value instanceof SetFile item) {
            var name = item.getName();
            if (name != null) addString(name);
        } else if (value instanceof StartLocal item) {
            var name = item.getName();
            if (name != null) addString(name);
            var type = item.getType();
            if (type != null) fillType(type);
            var signature = item.getSignature();
            if (signature != null) addString(signature);
        }
    }

    public void fillMethodDef(MethodDef value) {
        addString(value.getName());
        fillProto(value.getProto());
        var code = value.getImplementation();
        if (code != null) fillMethodImplementation(code);
        for (var tmp : value.getParameters()) {
            fillAnnotationSet(tmp.getAnnotations());
            if (debug) {
                var name = tmp.getName();
                if (name != null) addString(name);
            }
        }
        fillAnnotationSet(value.getAnnotations());
    }

    public void fillEncodedArray(EncodedArray value) {
        for (var tmp : value.getValue()) {
            fillEncodedValue(tmp);
        }
    }

    public void fillAnnotationElement(AnnotationElement value) {
        addString(value.getName());
        fillEncodedValue(value.getValue());
    }

    public void fillCommonAnnotation(CommonAnnotation value) {
        fillType(value.getType());
        for (var element : value.getElements()) {
            fillAnnotationElement(element);
        }
    }

    public void fillEncodedValue(EncodedValue raw) {
        if (raw instanceof EncodedString value) addString(value.getValue());
        if (raw instanceof EncodedType value) fillType(value.getValue());
        if (raw instanceof EncodedEnum value) fillField(value.getValue());
        if (raw instanceof EncodedField value) fillField(value.getValue());
        if (raw instanceof EncodedMethodType value) fillProto(value.getValue());
        if (raw instanceof EncodedMethod value) fillMethod(value.getValue());
        if (raw instanceof EncodedMethodHandle value) fillMethodHandle(value.getValue());
        if (raw instanceof EncodedAnnotation value) fillCommonAnnotation(value);
        if (raw instanceof EncodedArray value) fillEncodedArray(value);
    }
}
