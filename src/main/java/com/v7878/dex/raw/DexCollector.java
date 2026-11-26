package com.v7878.dex.raw;

import static com.v7878.dex.DexConstants.NO_OFFSET;

import com.v7878.dex.ReferenceType;
import com.v7878.dex.WriteOptions.RawFix;
import com.v7878.dex.WriteOptions.StringFix;
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
import com.v7878.dex.util.Converter;
import com.v7878.dex.util.IntegerPool;
import com.v7878.dex.util.ShortyUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.TreeMap;

public class DexCollector {
    private static final Integer NO_OFFSET_I = NO_OFFSET;

    public interface StringIndexer {
        int getStringCount();

        int getStringIndex(String value);
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

    public static final class CodeContainer {
        public final MethodImplementation value;
        public final DebugInfo debug_info;
        public final TryBlockContainer[] tries;
        public final int ins;
        public final int outs;
        public final boolean compact;

        public int debug_info_offset;

        private final int hash;

        public CodeContainer(MethodImplementation value, DebugInfo debug_info,
                             TryBlockContainer[] tries, int ins, int outs, boolean compact) {
            this.value = Objects.requireNonNull(value);
            this.debug_info = debug_info;
            this.tries = tries;
            this.ins = ins;
            this.outs = outs;
            this.compact = compact;

            this.debug_info_offset = -1;
            // For compact dex files, debug info is not compared
            int h = 1;
            h = 31 * h + Integer.hashCode(ins);
            h = 31 * h + Integer.hashCode(value.getRegisterCount());
            h = 31 * h + Objects.hashCode(value.getInstructions());
            h = 31 * h + Objects.hashCode(value.getTryBlocks());
            if (!compact) {
                h = 31 * h + Objects.hashCode(debug_info);
            }
            this.hash = h;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            return obj instanceof CodeContainer other
                    // This check is needed to speed up comparison
                    && hash == other.hash
                    && ins == other.ins
                    // 'outs' depends on instructions
                    // && outs == other.outs
                    && value.getRegisterCount() == other.value.getRegisterCount()
                    && Objects.equals(value.getInstructions(), other.value.getInstructions())
                    && Arrays.equals(tries, other.tries)
                    // This comparison is at the end because it should never fail
                    && compact == other.compact
                    // For compact dex files, debug info is not compared
                    && (compact || Objects.equals(debug_info, other.debug_info));
        }

        @Override
        public int hashCode() {
            return hash;
        }

        private static TryBlockContainer[] toTriesArray(NavigableSet<TryBlock> tries) {
            return Converter.transform(tries, TryBlockContainer::of, TryBlockContainer[]::new);
        }

        public static CodeContainer of(boolean compact, MethodImplementation value,
                                       DebugInfo debug_info, MethodId id, int flags) {
            if (value == null) return null;
            return new CodeContainer(value, debug_info, toTriesArray(value.getTryBlocks()),
                    ShortyUtils.getInputRegisterCount(id.getParameterTypes(), flags),
                    ShortyUtils.getOutputRegisters(value.getInstructions()), compact);
        }
    }

    public record MethodDefContainer(MethodDef value, MethodId id,
                                     CodeContainer code) {
        // equals and hashCode are not used

        private static List<String> toNamesList(List<Parameter> parameters) {
            return Converter.transformList(parameters, Parameter::getName);
        }

        private static DebugInfo toDebugInfo(List<String> names, List<DebugItem> items) {
            if (items.isEmpty()) {
                if (names.isEmpty()) {
                    return null;
                }
                boolean empty = true;
                for (int i = 0, n = names.size(); i < n; i++) {
                    if (names.get(i) != null) {
                        empty = false;
                        break;
                    }
                }
                if (empty) {
                    return null;
                }
            }
            return new DebugInfo(names, items);
        }

        public static MethodDefContainer of(StringIndexer strings, StringFix string_fix,
                                            RawFix raw_fix, boolean compact, boolean debug,
                                            TypeId declaring_class, MethodDef value) {
            var impl = value.getImplementation();
            if (impl != null) {
                impl = FixupRewriter.process(impl, strings, string_fix, raw_fix, debug);
            }
            var parameters = value.getParameters();
            var debug_info = (impl == null || !debug) ? null :
                    toDebugInfo(toNamesList(parameters), impl.getDebugItems());
            var id = MethodId.of(declaring_class, value.getName(),
                    value.getReturnType(), value.getParameterTypes());
            var code = CodeContainer.of(compact, impl, debug_info, id, value.getAccessFlags());
            return new MethodDefContainer(value, id, code);
        }
    }

    public static final class ClassDefContainer {
        public final ClassDef value;
        public final List<TypeId> interfaces;
        public final FieldDefContainer[] static_fields;
        public final EncodedArray static_values;
        public final FieldDefContainer[] instance_fields;
        public final MethodDefContainer[] direct_methods;
        public final MethodDefContainer[] virtual_methods;
        public final AnnotationDirectory annotations;

        public int offset;

        public ClassDefContainer(ClassDef value,
                                 List<TypeId> interfaces,
                                 FieldDefContainer[] static_fields,
                                 EncodedArray static_values,
                                 FieldDefContainer[] instance_fields,
                                 MethodDefContainer[] direct_methods,
                                 MethodDefContainer[] virtual_methods,
                                 AnnotationDirectory annotations) {
            this.value = value;
            this.interfaces = interfaces;
            this.static_fields = static_fields;
            this.static_values = static_values;
            this.instance_fields = instance_fields;
            this.direct_methods = direct_methods;
            this.virtual_methods = virtual_methods;
            this.annotations = annotations;

            this.offset = -1;
        }

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
                StringIndexer strings, StringFix string_fix, RawFix raw_fix, boolean compact,
                boolean debug, TypeId declaring_class, NavigableSet<MethodDef> methods) {
            return Converter.transform(methods,
                    value -> MethodDefContainer.of(strings, string_fix,
                            raw_fix, compact, debug, declaring_class, value),
                    MethodDefContainer[]::new);
        }

        private static EncodedArray toStaticValuesList(FieldDefContainer[] fields) {
            var out = Converter.minimize(Arrays.asList(fields),
                    FieldDefContainer::value, field -> {
                        var value = field.getInitialValue();
                        return value != null ? value : EncodedValue.defaultValue(field.getType());
                    }, field -> {
                        var value = field.getInitialValue();
                        return value == null || value.isDefault();
                    });
            return out.isEmpty() ? null : EncodedArray.raw(out);
        }

        public static ClassDefContainer of(StringIndexer strings, StringFix string_fix, RawFix raw_fix,
                                           boolean compact, boolean debug, ClassDef value) {
            var type = value.getType();
            var interfaces = value.getInterfaces();
            var static_fields = toFieldsArray(type, value.getStaticFields());
            var instance_fields = toFieldsArray(type, value.getInstanceFields());
            var direct_methods = toMethodsArray(strings, string_fix, raw_fix,
                    compact, debug, type, value.getDirectMethods());
            var virtual_methods = toMethodsArray(strings, string_fix, raw_fix,
                    compact, debug, type, value.getVirtualMethods());
            var annotations = AnnotationDirectory.of(value, static_fields,
                    instance_fields, direct_methods, virtual_methods);
            return new ClassDefContainer(value,
                    interfaces.isEmpty() ? null : interfaces,
                    static_fields, toStaticValuesList(static_fields),
                    instance_fields, direct_methods, virtual_methods,
                    annotations);
        }
    }

    public record AnnotationDirectory(
            TypeId declaring_class,
            NavigableSet<Annotation> class_annotations,
            NavigableMap<FieldId, NavigableSet<Annotation>> field_annotations,
            NavigableMap<MethodId, NavigableSet<Annotation>> method_annotations,
            NavigableMap<MethodId, List<NavigableSet<Annotation>>> parameter_annotations
    ) {
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            return obj instanceof AnnotationDirectory other
                    && Objects.equals(declaring_class, other.declaring_class)
                    && Objects.equals(class_annotations, other.class_annotations);
        }

        @Override
        public int hashCode() {
            int h = 1;
            h = 31 * h + Objects.hashCode(declaring_class);
            h = 31 * h + Objects.hashCode(class_annotations);
            return h;
        }

        private static List<NavigableSet<Annotation>> toAnnotationsList(List<Parameter> parameters) {
            var list = Converter.transformList(parameters, param -> {
                var anno = param.getAnnotations();
                return anno.isEmpty() ? null : anno;
            });
            boolean empty = true;
            for (int i = 0, n = list.size(); i < n; i++) {
                if (list.get(i) != null) {
                    empty = false;
                    break;
                }
            }
            return empty ? null : list;
        }

        private static void fill(
                FieldDefContainer[] fields,
                NavigableMap<FieldId, NavigableSet<Annotation>> field_annotations
        ) {
            for (var field : fields) {
                var annotations = field.value().getAnnotations();
                if (!annotations.isEmpty()) {
                    field_annotations.put(field.id(), annotations);
                }
            }
        }

        private static void fill(
                MethodDefContainer[] methods,
                NavigableMap<MethodId, NavigableSet<Annotation>> method_annotations,
                NavigableMap<MethodId, List<NavigableSet<Annotation>>> parameter_annotations
        ) {
            for (var method : methods) {
                var m_annotations = method.value().getAnnotations();
                if (!m_annotations.isEmpty()) {
                    method_annotations.put(method.id(), m_annotations);
                }
                var p_annotations = toAnnotationsList(method.value().getParameters());
                if (p_annotations != null) {
                    parameter_annotations.put(method.id(), p_annotations);
                }
            }
        }

        public static AnnotationDirectory of(ClassDef value,
                                             FieldDefContainer[] static_fields,
                                             FieldDefContainer[] instance_fields,
                                             MethodDefContainer[] direct_methods,
                                             MethodDefContainer[] virtual_methods) {
            var class_annotations = value.getAnnotations();
            class_annotations = class_annotations.isEmpty() ? null : class_annotations;
            var field_annotations = new TreeMap<FieldId, NavigableSet<Annotation>>();
            var method_annotations = new TreeMap<MethodId, NavigableSet<Annotation>>();
            var parameter_annotations = new TreeMap<MethodId, List<NavigableSet<Annotation>>>();
            fill(static_fields, field_annotations);
            fill(instance_fields, field_annotations);
            fill(direct_methods, method_annotations, parameter_annotations);
            fill(virtual_methods, method_annotations, parameter_annotations);
            // If an AnnotationDirectory contains only class annotations,
            // it can be used multiple times for different classes
            var type = field_annotations.isEmpty() && method_annotations.isEmpty() &&
                    parameter_annotations.isEmpty() ? null : value.getType();
            if (type == null && class_annotations == null) return null;
            return new AnnotationDirectory(type, class_annotations,
                    field_annotations, method_annotations, parameter_annotations);
        }
    }

    public final LinkedHashSet<TypeId> types;
    public final LinkedHashSet<ProtoId> protos;
    public final LinkedHashSet<FieldId> fields;
    public final LinkedHashSet<MethodId> methods;
    public final LinkedHashSet<MethodHandleId> method_handles;
    public final LinkedHashMap<CallSiteId, Integer> call_sites;

    public final LinkedHashMap<List<TypeId>, Integer> type_lists;
    public final LinkedHashMap<EncodedArray, Integer> encoded_arrays;
    public final LinkedHashMap<DebugInfo, Integer> debug_infos;
    public final LinkedHashMap<CodeContainer, Integer> code_items;
    public final LinkedHashMap<Annotation, Integer> annotations;
    public final LinkedHashMap<NavigableSet<Annotation>, Integer> annotation_sets;
    public final LinkedHashMap<List<NavigableSet<Annotation>>, Integer> annotation_set_lists;
    public final LinkedHashMap<AnnotationDirectory, Integer> annotation_directories;

    public final List<ClassDefContainer> class_defs;

    private final boolean compact;
    private final boolean debug;

    private final StringIndexer strings;
    private final StringFix string_fix;
    private final RawFix raw_fix;

    public DexCollector(StringIndexer strings, StringFix string_fix,
                        RawFix raw_fix, boolean compact, boolean debug) {
        this.strings = strings;
        this.string_fix = string_fix;
        this.raw_fix = raw_fix;
        this.compact = compact;
        this.debug = debug;

        types = new LinkedHashSet<>();
        protos = new LinkedHashSet<>();
        fields = new LinkedHashSet<>();
        methods = new LinkedHashSet<>();
        method_handles = new LinkedHashSet<>();
        call_sites = new LinkedHashMap<>();

        type_lists = new LinkedHashMap<>();
        encoded_arrays = new LinkedHashMap<>();
        debug_infos = new LinkedHashMap<>();
        code_items = new LinkedHashMap<>();
        annotations = new LinkedHashMap<>();
        annotation_sets = new LinkedHashMap<>();
        annotation_set_lists = new LinkedHashMap<>();
        annotation_directories = new LinkedHashMap<>();

        class_defs = new ArrayList<>();
    }

    public void addType(TypeId value) {
        types.add(value);
    }

    public void addField(FieldId value) {
        if (fields.add(value)) {
            addType(value.getDeclaringClass());
            addType(value.getType());
        }
    }

    public void addProto(ProtoId value) {
        if (protos.add(value)) {
            addType(value.getReturnType());
            addTypeList(value.getParameterTypes());
        }
    }

    public void addMethod(MethodId value) {
        if (methods.add(value)) {
            addType(value.getDeclaringClass());
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
        int index = call_sites.size();
        if (call_sites.putIfAbsent(value, IntegerPool.get(index)) == null) {
            addEncodedArray(value.getEncodedArray());
        }
    }

    public void addClassDef(ClassDef value) {
        var container = ClassDefContainer.of(strings, string_fix, raw_fix, compact, debug, value);
        class_defs.add(container);
        addType(value.getType());
        var superclass = value.getSuperclass();
        if (superclass != null) addType(superclass);
        var interfaces = container.interfaces;
        if (interfaces != null) addTypeList(interfaces);
        var static_values = container.static_values;
        if (static_values != null) addEncodedArray(static_values);
        for (var field : container.static_fields) {
            fillFieldDef(field);
        }
        for (var field : container.instance_fields) {
            fillFieldDef(field);
        }
        for (var method : container.direct_methods) {
            fillMethodDef(method);
        }
        for (var method : container.virtual_methods) {
            fillMethodDef(method);
        }
        var annotations = container.annotations;
        if (annotations != null) addAnnotationDirectory(annotations);
    }

    public void addAnnotation(Annotation value) {
        if (annotations.putIfAbsent(value, NO_OFFSET_I) == null) {
            fillCommonAnnotation(value);
        }
    }

    public void addAnnotationSet(NavigableSet<Annotation> value) {
        if (annotation_sets.putIfAbsent(value, NO_OFFSET_I) == null) {
            for (var tmp : value) {
                addAnnotation(tmp);
            }
        }
    }

    public void addAnnotationSetList(List<NavigableSet<Annotation>> value) {
        if (annotation_set_lists.putIfAbsent(value, NO_OFFSET_I) == null) {
            for (int i = 0, n = value.size(); i < n; i++) {
                var tmp = value.get(i);
                if (tmp != null) addAnnotationSet(tmp);
            }
        }
    }

    public void addAnnotationDirectory(AnnotationDirectory value) {
        if (annotation_directories.putIfAbsent(value, NO_OFFSET_I) == null) {
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
        if (type_lists.putIfAbsent(value, NO_OFFSET_I) == null) {
            for (int i = 0, n = value.size(); i < n; i++) {
                addType(value.get(i));
            }
        }
    }

    public void addEncodedArray(EncodedArray value) {
        if (encoded_arrays.putIfAbsent(value, NO_OFFSET_I) == null) {
            fillEncodedArray(value);
        }
    }

    public void addDebugInfo(DebugInfo value) {
        assert debug;
        if (debug_infos.putIfAbsent(value, NO_OFFSET_I) == null) {
            var items = value.items();
            for (int i = 0, n = items.size(); i < n; i++) {
                fillDebugItem(items.get(i));
            }
        }
    }

    public void addCodeItem(CodeContainer value) {
        if (code_items.putIfAbsent(value, NO_OFFSET_I) == null) {
            var instructions = value.value.getInstructions();
            for (int i = 0, n = instructions.size(); i < n; i++) {
                fillInstruction(instructions.get(i));
            }
            for (var tmp : value.value.getTryBlocks()) {
                fillTryBlock(tmp);
            }
        }
        // For compact dex files, equal code items with different debug
        // info share one code item, but every method still refers to its
        // own debug info
        var debug_info = value.debug_info;
        if (debug_info != null) addDebugInfo(debug_info);
    }

    public void fillDex(Dex value) {
        var classes = value.getClasses();
        for (int i = 0, n = classes.size(); i < n; i++) {
            addClassDef(classes.get(i));
        }
    }

    public void collect(ReferenceType type, Object value) {
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
        var handlers = value.getHandlers();
        for (int i = 0, n = handlers.size(); i < n; i++) {
            fillExceptionHandler(handlers.get(i));
        }
    }

    public void fillFieldDef(FieldDefContainer value) {
        addField(value.id());
    }

    public void fillDebugItem(DebugItem value) {
        assert debug;
        if (value instanceof StartLocal item) {
            var type = item.getType();
            if (type != null) addType(type);
        }
    }

    public void fillMethodDef(MethodDefContainer value) {
        addMethod(value.id());
        var code = value.code();
        if (code != null) addCodeItem(code);
    }

    public void fillEncodedArray(EncodedArray value) {
        var values = value.getValue();
        for (int i = 0, n = values.size(); i < n; i++) {
            fillEncodedValue(values.get(i));
        }
    }

    public void fillAnnotationElement(AnnotationElement value) {
        fillEncodedValue(value.getValue());
    }

    public void fillCommonAnnotation(CommonAnnotation value) {
        addType(value.getType());
        for (var element : value.getElements()) {
            fillAnnotationElement(element);
        }
    }

    public void fillEncodedValue(EncodedValue raw) {
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
