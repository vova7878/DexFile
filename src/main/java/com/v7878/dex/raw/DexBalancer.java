package com.v7878.dex.raw;

import com.v7878.dex.ReferenceType;
import com.v7878.dex.WriteOptions;
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
import com.v7878.dex.immutable.debug.StartLocal;
import com.v7878.dex.immutable.value.EncodedAnnotation;
import com.v7878.dex.immutable.value.EncodedArray;
import com.v7878.dex.immutable.value.EncodedEnum;
import com.v7878.dex.immutable.value.EncodedField;
import com.v7878.dex.immutable.value.EncodedMethod;
import com.v7878.dex.immutable.value.EncodedMethodHandle;
import com.v7878.dex.immutable.value.EncodedMethodType;
import com.v7878.dex.immutable.value.EncodedType;
import com.v7878.dex.immutable.value.EncodedValue;

import java.util.Collection;
import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class DexBalancer {
    private final Set<TypeId> types;
    private final Set<ProtoId> protos;
    private final Set<FieldId> fields;
    private final Set<MethodId> methods;
    private final Set<MethodHandleId> method_handles;
    private final Set<CallSiteId> call_sites;

    private final boolean debug;

    private static boolean check(Collection<?> collection) {
        return collection.size() > 65535;
    }

    private boolean check() {
        return check(types) || check(protos) || check(fields) ||
                check(methods) || check(method_handles) || check(call_sites);
    }

    private DexBalancer(boolean debug) {
        this.types = new TreeSet<>();
        this.protos = new TreeSet<>();
        this.fields = new TreeSet<>();
        this.methods = new TreeSet<>();
        this.method_handles = new TreeSet<>();
        this.call_sites = new TreeSet<>();
        this.debug = debug;
    }

    public static int balance(List<ClassDef> classes, List<Dex> dexes,
                              WriteOptions options, boolean last) {
        DexBalancer balancer = null;
        final int classes_count = classes.size();
        int start = 0;
        for (int i = 0; i < classes_count; i++) {
            if (balancer == null) {
                balancer = new DexBalancer(options.hasDebugInfo());
            }
            balancer.fillClassDef(classes.get(i));
            if (balancer.check()) {
                balancer = null;
                dexes.add(Dex.of(classes.subList(start, i)));
                start = i--;
            }
        }
        if (last && start != classes_count) {
            dexes.add(Dex.of(classes.subList(start, classes_count)));
            return classes_count;
        }
        return start;
    }

    private void addType(TypeId value) {
        types.add(value);
    }

    private void addField(FieldId value) {
        if (fields.add(value)) {
            addType(value.getDeclaringClass());
            addType(value.getType());
        }
    }

    private void addProto(ProtoId value) {
        if (protos.add(value)) {
            addType(value.getReturnType());
            fillTypeList(value.getParameterTypes());
        }
    }

    private void addMethod(MethodId value) {
        if (methods.add(value)) {
            addType(value.getDeclaringClass());
            addProto(value.getProto());
        }
    }

    private void addMethodHandle(MethodHandleId value) {
        if (method_handles.add(value)) {
            var member = value.getMember();
            if (value.getHandleType().isMethodAccess()) {
                addMethod((MethodId) member);
            } else {
                addField((FieldId) member);
            }
        }
    }

    private void addCallSite(CallSiteId value) {
        if (call_sites.add(value)) {
            addMethodHandle(value.getMethodHandle());
            addProto(value.getMethodProto());
            fillEncodedArray(EncodedArray.raw(value.getExtraArguments()));
        }
    }

    private void fillClassDef(ClassDef value) {
        addType(value.getType());
        var superclass = value.getSuperclass();
        if (superclass != null) addType(superclass);
        fillTypeList(value.getInterfaces());
        for (var field : value.getFields()) {
            fillFieldDef(value.getType(), field);
        }
        for (var method : value.getMethods()) {
            fillMethodDef(value.getType(), method);
        }
        fillAnnotationSet(value.getAnnotations());
    }

    private void fillAnnotationSet(NavigableSet<Annotation> value) {
        for (var tmp : value) {
            fillCommonAnnotation(tmp);
        }
    }

    private void fillTypeList(List<TypeId> value) {
        for (var tmp : value) {
            addType(tmp);
        }
    }

    private void fillDebugInfo(List<DebugItem> value) {
        for (var tmp : value) {
            fillDebugItem(tmp);
        }
    }

    private void fillMethodImplementation(MethodImplementation value) {
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

    private void fillReference(ReferenceType type, Object value) {
        Objects.requireNonNull(value);
        switch (type) {
            case TYPE -> addType((TypeId) value);
            case FIELD -> addField((FieldId) value);
            case METHOD -> addMethod((MethodId) value);
            case PROTO -> addProto((ProtoId) value);
            case CALLSITE -> addCallSite((CallSiteId) value);
            case METHOD_HANDLE -> addMethodHandle((MethodHandleId) value);
            case STRING, RAW_INDEX -> { /* nop */ }
        }
    }

    private void fillInstruction(Instruction value) {
        if (value instanceof SingleReferenceInstruction ref1) {
            fillReference(ref1.getReferenceType1(), ref1.getReference1());
        }
        if (value instanceof DualReferenceInstruction ref2) {
            fillReference(ref2.getReferenceType2(), ref2.getReference2());
        }
    }

    private void fillExceptionHandler(ExceptionHandler value) {
        addType(value.getExceptionType());
    }

    private void fillTryBlock(TryBlock value) {
        for (var tmp : value.getHandlers()) {
            fillExceptionHandler(tmp);
        }
    }

    private void fillFieldDef(TypeId declaring_class, FieldDef value) {
        addField(FieldId.of(declaring_class, value.getName(), value.getType()));
        var initial_value = value.getInitialValue();
        if (initial_value != null) fillEncodedValue(value.getInitialValue());
        fillAnnotationSet(value.getAnnotations());
    }

    private void fillDebugItem(DebugItem value) {
        if (value instanceof StartLocal item) {
            var type = item.getType();
            if (type != null) addType(type);
        }
    }

    private void fillMethodDef(TypeId declaring_class, MethodDef value) {
        addMethod(MethodId.of(declaring_class, value.getName(), value.getProto()));
        var code = value.getImplementation();
        if (code != null) fillMethodImplementation(code);
        for (var tmp : value.getParameters()) {
            fillAnnotationSet(tmp.getAnnotations());
        }
        fillAnnotationSet(value.getAnnotations());
    }

    private void fillEncodedArray(EncodedArray value) {
        for (var tmp : value.getValue()) {
            fillEncodedValue(tmp);
        }
    }

    private void fillAnnotationElement(AnnotationElement value) {
        fillEncodedValue(value.getValue());
    }

    private void fillCommonAnnotation(CommonAnnotation value) {
        addType(value.getType());
        for (var element : value.getElements()) {
            fillAnnotationElement(element);
        }
    }

    private void fillEncodedValue(EncodedValue raw) {
        if (raw instanceof EncodedType value) addType(value.getValue());
        if (raw instanceof EncodedEnum value) addField(value.getValue());
        if (raw instanceof EncodedField value) addField(value.getValue());
        if (raw instanceof EncodedMethodType value) addProto(value.getValue());
        if (raw instanceof EncodedMethod value) addMethod(value.getValue());
        if (raw instanceof EncodedMethodHandle value) addMethodHandle(value.getValue());
        if (raw instanceof EncodedAnnotation value) fillCommonAnnotation(value);
        if (raw instanceof EncodedArray value) fillEncodedArray(value);
    }
}
