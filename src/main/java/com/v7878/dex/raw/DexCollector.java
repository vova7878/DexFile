package com.v7878.dex.raw;

import static com.v7878.dex.DexConstants.NO_OFFSET;

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
import com.v7878.dex.immutable.Parameter;
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
import com.v7878.dex.util.CollectionUtils;
import com.v7878.dex.util.Converter;
import com.v7878.dex.util.ShortyUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.TreeMap;
import java.util.TreeSet;

public class DexCollector {
    private static final Integer NO_OFFSET_I = NO_OFFSET;

    public record CallSiteIdContainer(CallSiteId value, EncodedArray array)
            implements Comparable<CallSiteIdContainer> {
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            return obj instanceof CallSiteIdContainer that
                    && Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }

        @Override
        public int compareTo(CallSiteIdContainer other) {
            return CollectionUtils.compareNonNull(value, other.value);
        }

        private static List<EncodedValue> toList(CallSiteId value) {
            Objects.requireNonNull(value);
            var extra_args = value.getExtraArguments();
            var list = new ArrayList<EncodedValue>(extra_args.size() + 3);
            list.add(EncodedMethodHandle.of(value.getMethodHandle()));
            list.add(EncodedString.of(value.getMethodName()));
            list.add(EncodedMethodType.of(value.getMethodProto()));
            list.addAll(extra_args);
            return list;
        }

        public static CallSiteIdContainer of(CallSiteId callsite) {
            return new CallSiteIdContainer(callsite, EncodedArray.raw(toList(callsite)));
        }
    }

    public record FieldDefContainer(FieldDef value, FieldId id) {
        // equals and hashCode are not used

        public static FieldDefContainer of(TypeId declaring_class, FieldDef value) {
            return new FieldDefContainer(value, FieldId.of(
                    declaring_class, value.getName(), value.getType()));
        }
    }

    public record TryBlockContainer(TryBlock value, CatchHandler handler) {
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            return obj instanceof TryBlockContainer that
                    && Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }

        public static TryBlockContainer of(TryBlock value) {
            return new TryBlockContainer(value, new CatchHandler(
                    value.getHandlers(), value.getCatchAllAddress()));
        }
    }

    public record CodeContainer(MethodImplementation value,
                                // Not null only for standard dex files
                                DebugInfo debug_info,
                                TryBlockContainer[] tries,
                                int ins, int outs) {
        public CodeContainer {
            Objects.requireNonNull(value);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            return obj instanceof CodeContainer other
                    && ins == other.ins
                    // 'outs' depends on instructions, exists because it can speed up the comparison
                    && outs == other.outs
                    && value.getRegisterCount() == other.value.getRegisterCount()
                    && Objects.equals(value.getInstructions(), other.value.getInstructions())
                    && Arrays.equals(tries, other.tries)
                    && Objects.equals(debug_info, other.debug_info);
        }

        @Override
        public int hashCode() {
            return Objects.hash(ins, /* outs (depends on instructions), */ value.getRegisterCount(),
                    value.getInstructions(), value.getTryBlocks(), debug_info);
        }

        private static TryBlockContainer[] toTriesArray(NavigableSet<TryBlock> tries) {
            return Converter.transform(tries, TryBlockContainer::of, TryBlockContainer[]::new);
        }

        public static CodeContainer of(MethodImplementation value,
                                       DebugInfo debug_info,
                                       MethodId id, int flags) {
            if (value == null) return null;
            return new CodeContainer(value, debug_info, toTriesArray(value.getTryBlocks()),
                    ShortyUtils.getInputRegisterCount(id.getParameterTypes(), flags),
                    ShortyUtils.getOutputRegisters(value.getInstructions()));
        }
    }

    public record MethodDefContainer(MethodDef value, MethodId id,
                                     // Not null only for compact dex files
                                     DebugInfo debug_info,
                                     CodeContainer code) {
        // equals and hashCode are not used

        // TODO: simplify
        private static List<String> toNamesList(List<Parameter> parameters) {
            int size = parameters.size();
            for (; size > 0; size--) {
                var name = parameters.get(size - 1).getName();
                if (name != null) break;
            }
            if (size == 0) {
                return Collections.emptyList();
            }
            var out = new ArrayList<String>(size);
            for (int i = 0; i < size; i++) {
                out.add(parameters.get(i).getName());
            }
            return out;
        }

        private static DebugInfo toDebugInfo(List<String> names, List<DebugItem> items) {
            if (names.isEmpty() && items.isEmpty()) {
                return null;
            }
            return new DebugInfo(names, items);
        }

        public static MethodDefContainer of(boolean is_compact, boolean collect_debug_info,
                                            TypeId declaring_class, MethodDef value) {
            var code = value.getImplementation();
            var parameters = value.getParameters();
            var debug_info = (code == null || !collect_debug_info) ? null :
                    toDebugInfo(toNamesList(parameters), code.getDebugItems());
            MethodId id = MethodId.of(declaring_class, value.getName(),
                    value.getReturnType(), value.getParameterTypes());
            return new MethodDefContainer(value, id, is_compact ? debug_info : null,
                    CodeContainer.of(code, is_compact ? null : debug_info, id, value.getAccessFlags()));
        }
    }

    public record ClassDefContainer(ClassDef value,
                                    List<TypeId> interfaces,
                                    FieldDefContainer[] static_fields,
                                    EncodedArray static_values,
                                    FieldDefContainer[] instance_fields,
                                    MethodDefContainer[] direct_methods,
                                    MethodDefContainer[] virtual_methods,
                                    AnnotationDirectory annotations) {
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            return obj instanceof ClassDefContainer that
                    && Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }

        public boolean isEmptyClassData() {
            return static_fields.length == 0 && instance_fields.length == 0
                    && direct_methods.length == 0 && virtual_methods.length == 0;
        }

        private static FieldDefContainer[] toFieldsArray(
                TypeId declaring_class, NavigableSet<FieldDef> fields) {
            return Converter.transform(fields,
                    value -> FieldDefContainer.of(declaring_class, value),
                    FieldDefContainer[]::new);
        }

        private static MethodDefContainer[] toMethodsArray(
                boolean is_compact, boolean collect_debug_info,
                TypeId declaring_class, NavigableSet<MethodDef> methods) {
            return Converter.transform(methods,
                    value -> MethodDefContainer.of(is_compact,
                            collect_debug_info, declaring_class, value),
                    MethodDefContainer[]::new);
        }

        // TODO: simplify
        private static EncodedArray toStaticValuesList(FieldDefContainer[] fields) {
            int size = fields.length;
            for (; size > 0; size--) {
                var value = fields[size - 1].value().getInitialValue();
                if (value != null && !value.isDefault()) break;
            }
            if (size == 0) {
                return null;
            }
            var out = new ArrayList<EncodedValue>(size);
            for (int i = 0; i < size; i++) {
                var value = fields[i].value().getInitialValue();
                out.add(value != null ? value : EncodedValue
                        .defaultValue(fields[i].value().getType()));
            }
            return EncodedArray.of(out);
        }

        public static ClassDefContainer of(boolean is_compact, boolean collect_debug_info, ClassDef value) {
            var type = value.getType();
            var interfaces = value.getInterfaces();
            var static_fields = toFieldsArray(type, value.getStaticFields());
            var instance_fields = toFieldsArray(type, value.getInstanceFields());
            var direct_methods = toMethodsArray(is_compact,
                    collect_debug_info, type, value.getDirectMethods());
            var virtual_methods = toMethodsArray(is_compact,
                    collect_debug_info, type, value.getVirtualMethods());
            var annotations = AnnotationDirectory.of(value, static_fields,
                    instance_fields, direct_methods, virtual_methods);
            return new ClassDefContainer(value,
                    interfaces.isEmpty() ? null : interfaces,
                    static_fields, toStaticValuesList(static_fields),
                    instance_fields, direct_methods, virtual_methods,
                    annotations.isEmpty() ? null : annotations);
        }
    }

    public record AnnotationDirectory(
            NavigableSet<Annotation> class_annotations,
            NavigableMap<FieldId, NavigableSet<Annotation>> field_annotations,
            NavigableMap<MethodId, NavigableSet<Annotation>> method_annotations,
            NavigableMap<MethodId, List<NavigableSet<Annotation>>> parameter_annotations
    ) {
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            return obj instanceof AnnotationDirectory that
                    && isEmptyExceptForClass() && that.isEmptyExceptForClass()
                    && Objects.equals(class_annotations, that.class_annotations);
        }

        @Override
        public int hashCode() {
            return isEmptyExceptForClass() ?
                    Objects.hashCode(class_annotations) :
                    // TODO: type id hash
                    System.identityHashCode(this);
        }

        public boolean isEmpty() {
            return class_annotations == null && isEmptyExceptForClass();
        }

        public boolean isEmptyExceptForClass() {
            return field_annotations.isEmpty() &&
                    method_annotations.isEmpty() &&
                    parameter_annotations.isEmpty();
        }

        // TODO: simplify
        private static List<NavigableSet<Annotation>> toAnnotationsList(List<Parameter> parameters) {
            int size = parameters.size();
            for (; size > 0; size--) {
                var annotations = parameters.get(size - 1).getAnnotations();
                if (!annotations.isEmpty()) break;
            }
            if (size == 0) {
                return null;
            }
            var out = new ArrayList<NavigableSet<Annotation>>(size);
            for (int i = 0; i < size; i++) {
                var annotations = parameters.get(i).getAnnotations();
                out.add(annotations.isEmpty() ? null : annotations);
            }
            return out;
        }

        private static void fill(AnnotationDirectory dir, FieldDefContainer[] fields) {
            for (var field : fields) {
                var annotations = field.value().getAnnotations();
                if (!annotations.isEmpty()) {
                    dir.field_annotations.put(field.id(), annotations);
                }
            }
        }

        private static void fill(AnnotationDirectory dir, MethodDefContainer[] methods) {
            for (var method : methods) {
                var method_annotations = method.value().getAnnotations();
                if (!method_annotations.isEmpty()) {
                    dir.method_annotations.put(method.id(), method_annotations);
                }
                var parameter_annotations = toAnnotationsList(method.value().getParameters());
                if (parameter_annotations != null) {
                    dir.parameter_annotations.put(method.id(), parameter_annotations);
                }
            }
        }

        public static AnnotationDirectory of(ClassDef value,
                                             FieldDefContainer[] static_fields,
                                             FieldDefContainer[] instance_fields,
                                             MethodDefContainer[] direct_methods,
                                             MethodDefContainer[] virtual_methods) {
            var class_annotations = value.getAnnotations();
            var out = new AnnotationDirectory(
                    class_annotations.isEmpty() ? null : class_annotations,
                    new TreeMap<>(), new TreeMap<>(), new TreeMap<>());
            fill(out, static_fields);
            fill(out, instance_fields);
            fill(out, direct_methods);
            fill(out, virtual_methods);
            return out;
        }
    }

    public final NavigableSet<String> strings;
    public final NavigableSet<TypeId> types;
    public final NavigableSet<ProtoId> protos;
    public final NavigableSet<FieldId> fields;
    public final NavigableSet<MethodId> methods;
    public final NavigableSet<MethodHandleId> method_handles;
    public final NavigableSet<CallSiteIdContainer> call_sites;

    public final Map<List<TypeId>, Integer> type_lists;
    public final Map<EncodedArray, Integer> encoded_arrays;
    public final Map<DebugInfo, Integer> debug_infos;
    public final Map<CodeContainer, Integer> code_items;
    public final Map<Annotation, Integer> annotations;
    public final Map<NavigableSet<Annotation>, Integer> annotation_sets;
    public final Map<List<NavigableSet<Annotation>>, Integer> annotation_set_lists;
    public final Map<AnnotationDirectory, Integer> annotation_directories;

    public final List<ClassDefContainer> class_defs;

    private final boolean is_compact;
    private final boolean collect_debug_info;

    public DexCollector(boolean is_compact, boolean collect_debug_info) {
        this.is_compact = is_compact;
        this.collect_debug_info = collect_debug_info;

        strings = new TreeSet<>();
        types = new TreeSet<>();
        protos = new TreeSet<>();
        fields = new TreeSet<>();
        methods = new TreeSet<>();
        method_handles = new TreeSet<>();
        call_sites = new TreeSet<>();

        type_lists = new HashMap<>();
        encoded_arrays = new HashMap<>();
        debug_infos = new HashMap<>();
        code_items = new HashMap<>();
        annotations = new HashMap<>();
        annotation_sets = new HashMap<>();
        annotation_set_lists = new HashMap<>();
        annotation_directories = new HashMap<>();

        class_defs = new ArrayList<>();
    }

    public void addString(String value) {
        strings.add(value);
    }

    public void addType(TypeId value) {
        if (types.add(value)) {
            addString(value.getDescriptor());
        }
    }

    public void addField(FieldId value) {
        if (fields.add(value)) {
            addType(value.getDeclaringClass());
            addString(value.getName());
            addType(value.getType());
        }
    }

    public void addProto(ProtoId value) {
        if (protos.add(value)) {
            addString(value.computeShorty());
            addType(value.getReturnType());
            addTypeList(value.getParameterTypes());
        }
    }

    public void addMethod(MethodId value) {
        if (methods.add(value)) {
            addType(value.getDeclaringClass());
            addString(value.getName());
            addProto(value.getProto());
        }
    }

    public void addMethodHandle(MethodHandleId value) {
        if (method_handles.add(value)) {
            var member = value.getMember();
            if (value.getHandleType().isMethodAccess()) {
                addMethod((MethodId) member);
            } else {
                addField((FieldId) member);
            }
        }
    }

    public void addCallSite(CallSiteId value) {
        var container = CallSiteIdContainer.of(value);
        if (call_sites.add(container)) {
            addEncodedArray(container.array());
        }
    }

    public void addClassDef(ClassDef value) {
        var container = ClassDefContainer.of(is_compact, collect_debug_info, value);
        class_defs.add(container);
        addType(value.getType());
        var superclass = value.getSuperclass();
        if (superclass != null) addType(superclass);
        var interfaces = container.interfaces();
        if (interfaces != null) addTypeList(interfaces);
        var source_file = value.getSourceFile();
        if (source_file != null) addString(source_file);
        var static_values = container.static_values();
        if (static_values != null) addEncodedArray(static_values);
        for (var field : container.static_fields()) {
            fillFieldDef(field);
        }
        for (var field : container.instance_fields()) {
            fillFieldDef(field);
        }
        for (var method : container.direct_methods()) {
            fillMethodDef(method);
        }
        for (var method : container.virtual_methods()) {
            fillMethodDef(method);
        }
        var annotations = container.annotations();
        if (annotations != null) addAnnotationDirectory(annotations);
    }

    public void addAnnotation(Annotation value) {
        if (annotations.put(value, NO_OFFSET_I) == null) {
            fillCommonAnnotation(value);
        }
    }

    public void addAnnotationSet(NavigableSet<Annotation> value) {
        if (annotation_sets.put(value, NO_OFFSET_I) == null) {
            for (var tmp : value) {
                addAnnotation(tmp);
            }
        }
    }

    public void addAnnotationSetList(List<NavigableSet<Annotation>> value) {
        if (annotation_set_lists.put(value, NO_OFFSET_I) == null) {
            for (var tmp : value) {
                if (tmp != null) addAnnotationSet(tmp);
            }
        }
    }

    public void addAnnotationDirectory(AnnotationDirectory value) {
        if (annotation_directories.put(value, NO_OFFSET_I) == null) {
            var class_annotations = value.class_annotations();
            if (class_annotations != null) addAnnotationSet(class_annotations);
            for (var entry : value.field_annotations.entrySet()) {
                addAnnotationSet(entry.getValue());
            }
            for (var entry : value.method_annotations.entrySet()) {
                addAnnotationSet(entry.getValue());
            }
            for (var entry : value.parameter_annotations.entrySet()) {
                addAnnotationSetList(entry.getValue());
            }
        }
    }

    public void addTypeList(List<TypeId> value) {
        if (type_lists.put(value, NO_OFFSET_I) == null) {
            for (var tmp : value) {
                addType(tmp);
            }
        }
    }

    public void addEncodedArray(EncodedArray value) {
        if (encoded_arrays.put(value, NO_OFFSET_I) == null) {
            fillEncodedArray(value);
        }
    }

    public void addDebugInfo(DebugInfo value) {
        assert collect_debug_info;
        if (debug_infos.put(value, NO_OFFSET_I) == null) {
            for (var tmp : value.parameter_names()) {
                if (tmp != null) addString(tmp);
            }
            for (var tmp : value.items()) {
                fillDebugItem(tmp);
            }
        }
    }

    public void addCodeItem(CodeContainer value) {
        if (code_items.put(value, NO_OFFSET_I) == null) {
            for (var tmp : value.value().getInstructions()) {
                fillInstruction(tmp);
            }
            for (var tmp : value.value().getTryBlocks()) {
                fillTryBlock(tmp);
            }
            var debug_info = value.debug_info();
            if (debug_info != null) addDebugInfo(debug_info);
        }
    }

    public void fillDex(Dex value) {
        for (var tmp : value.getClasses()) {
            addClassDef(tmp);
        }
    }

    public void collect(ReferenceType type, Object value) {
        Objects.requireNonNull(value);
        switch (type) {
            case STRING -> addString((String) value);
            case TYPE -> addType((TypeId) value);
            case FIELD -> addField((FieldId) value);
            case METHOD -> addMethod((MethodId) value);
            case PROTO -> addProto((ProtoId) value);
            case CALLSITE -> addCallSite((CallSiteId) value);
            case METHOD_HANDLE -> addMethodHandle((MethodHandleId) value);
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
        addType(value.getExceptionType());
    }

    public void fillTryBlock(TryBlock value) {
        for (var tmp : value.getHandlers()) {
            fillExceptionHandler(tmp);
        }
    }

    public void fillFieldDef(FieldDefContainer value) {
        addField(value.id());
    }

    public void fillDebugItem(DebugItem value) {
        assert collect_debug_info;
        if (value instanceof SetFile item) {
            var name = item.getName();
            if (name != null) addString(name);
        } else if (value instanceof StartLocal item) {
            var name = item.getName();
            if (name != null) addString(name);
            var type = item.getType();
            if (type != null) addType(type);
            var signature = item.getSignature();
            if (signature != null) addString(signature);
        }
    }

    public void fillMethodDef(MethodDefContainer value) {
        addMethod(value.id());
        var code = value.code();
        if (code != null) addCodeItem(code);
        var debug_info = value.debug_info();
        if (debug_info != null) addDebugInfo(debug_info);
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
        addType(value.getType());
        for (var element : value.getElements()) {
            fillAnnotationElement(element);
        }
    }

    public void fillEncodedValue(EncodedValue raw) {
        if (raw instanceof EncodedString value) addString(value.getValue());
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
