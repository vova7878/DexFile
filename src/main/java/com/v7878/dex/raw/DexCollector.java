package com.v7878.dex.raw;

import static com.v7878.dex.ReferenceType.ReferenceCollector;

import com.v7878.dex.immutable.Annotation;
import com.v7878.dex.immutable.AnnotationElement;
import com.v7878.dex.immutable.CallSiteId;
import com.v7878.dex.immutable.ClassDef;
import com.v7878.dex.immutable.CommonAnnotation;
import com.v7878.dex.immutable.Dex;
import com.v7878.dex.immutable.FieldDef;
import com.v7878.dex.immutable.FieldId;
import com.v7878.dex.immutable.MethodDef;
import com.v7878.dex.immutable.MethodHandleId;
import com.v7878.dex.immutable.MethodId;
import com.v7878.dex.immutable.Parameter;
import com.v7878.dex.immutable.ProtoId;
import com.v7878.dex.immutable.TypeId;
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
import com.v7878.dex.util.ItemConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.TreeSet;

public class DexCollector implements ReferenceCollector {
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
            return value.compareTo(other.value);
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
            return new CallSiteIdContainer(callsite, EncodedArray.of(toList(callsite)));
        }
    }

    public record FieldDefContainer(FieldDef value, FieldId id)
            implements Comparable<FieldDefContainer> {
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            return obj instanceof FieldDefContainer that
                    && Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }

        @Override
        public int compareTo(FieldDefContainer other) {
            return value.compareTo(other.value);
        }

        public static FieldDefContainer of(TypeId declaring_class, FieldDef value) {
            return new FieldDefContainer(value, FieldId.of(
                    declaring_class, value.getName(), value.getType()));
        }
    }

    public record MethodDefContainer(MethodDef value, MethodId id, List<String> parameter_names,
                                     List<NavigableSet<Annotation>> parameter_annotations)
            implements Comparable<MethodDefContainer> {
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            return obj instanceof MethodDefContainer that
                    && Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }

        @Override
        public int compareTo(MethodDefContainer other) {
            return value.compareTo(other.value);
        }

        private static List<TypeId> toTypeList(List<Parameter> parameters) {
            return parameters.stream().map(Parameter::getType).toList();
        }

        private static List<String> toNamesList(List<Parameter> parameters) {
            return parameters.stream().map(Parameter::getName).toList();
        }

        private static List<NavigableSet<Annotation>> toAnnotationsList(List<Parameter> parameters) {
            return parameters.stream().map(Parameter::getAnnotations).toList();
        }

        public static MethodDefContainer of(TypeId declaring_class, MethodDef value) {
            var parameters = value.getParameters();
            return new MethodDefContainer(value, MethodId.of(declaring_class, value.getName(),
                    value.getReturnType(), toTypeList(parameters)),
                    toNamesList(parameters), toAnnotationsList(parameters));
        }
    }

    public record ClassDefContainer(ClassDef value,
                                    List<TypeId> interfaces,
                                    FieldDefContainer[] static_fields,
                                    EncodedArray static_values,
                                    FieldDefContainer[] instance_fields,
                                    MethodDefContainer[] direct_methods,
                                    MethodDefContainer[] virtual_methods) {
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

        private static FieldDefContainer[] toFieldsArray(
                TypeId declaring_class, NavigableSet<FieldDef> fields) {
            return fields.stream().map(value ->
                            FieldDefContainer.of(declaring_class, value))
                    .toArray(FieldDefContainer[]::new);
        }

        private static EncodedArray toStaticValuesList(NavigableSet<FieldDef> fields) {
            //TODO: trim default values, null if contains only defaults
            return EncodedArray.of(fields.stream().map(FieldDef::getInitialValue).toList());
        }

        private static MethodDefContainer[] toMethodsArray(
                TypeId declaring_class, NavigableSet<MethodDef> methods) {
            return methods.stream().map(value ->
                            MethodDefContainer.of(declaring_class, value))
                    .toArray(MethodDefContainer[]::new);
        }

        public boolean isEmptyClassData() {
            return static_fields.length == 0 && instance_fields.length == 0
                    && direct_methods.length == 0 && virtual_methods.length == 0;
        }

        public static ClassDefContainer of(ClassDef value) {
            var type = value.getType();
            var static_fields = value.getStaticFields();
            return new ClassDefContainer(value,
                    ItemConverter.toList(value.getInterfaces()),
                    toFieldsArray(type, static_fields),
                    toStaticValuesList(static_fields),
                    toFieldsArray(type, value.getInstanceFields()),
                    toMethodsArray(type, value.getDirectMethods()),
                    toMethodsArray(type, value.getVirtualMethods()));
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
    //public final Set<CodeItem> code_items;
    //public final Set<Annotation> annotations;
    //public final Set<NavigableSet<Annotation>> annotation_sets;
    //public final Set<List<NavigableSet<Annotation>>> annotation_set_lists;
    //public final Set<AnnotationDirectory> annotations_directories;

    public final List<ClassDefContainer> class_defs;

    public DexCollector() {
        strings = new TreeSet<>(CollectionUtils.naturalOrder());
        types = new TreeSet<>(CollectionUtils.naturalOrder());
        protos = new TreeSet<>(CollectionUtils.naturalOrder());
        fields = new TreeSet<>(CollectionUtils.naturalOrder());
        methods = new TreeSet<>(CollectionUtils.naturalOrder());
        method_handles = new TreeSet<>(CollectionUtils.naturalOrder());
        call_sites = new TreeSet<>(CollectionUtils.naturalOrder());

        type_lists = new HashMap<>();
        encoded_arrays = new HashMap<>();
        //code_items = new HashSet<>();
        //annotations = new HashSet<>();
        //annotation_sets = new HashSet<>();
        //annotation_set_lists = new HashSet<>();
        //annotations_directories = new HashSet<>();

        class_defs = new ArrayList<>();
    }

    @Override
    public void add(String value) {
        strings.add(value);
    }

    @Override
    public void add(TypeId value) {
        types.add(value);
        add(value.getDescriptor());
    }

    @Override
    public void add(FieldId value) {
        fields.add(value);
        add(value.getDeclaringClass());
        add(value.getName());
        add(value.getType());
    }

    @Override
    public void add(ProtoId value) {
        protos.add(value);
        add(value.getReturnType());
        add(value.getParameterTypes());
    }

    @Override
    public void add(MethodId value) {
        methods.add(value);
        add(value.getDeclaringClass());
        add(value.getName());
        add(value.getProto());
    }

    @Override
    public void add(MethodHandleId value) {
        method_handles.add(value);
        var member = value.getMember();
        if (value.getHandleType().isMethodAccess()) {
            add((MethodId) member);
        } else {
            add((FieldId) member);
        }
    }

    @Override
    public void add(CallSiteId value) {
        var container = CallSiteIdContainer.of(value);
        call_sites.add(container);
        add(container.array());
    }

    public void add(ClassDef value) {
        var container = ClassDefContainer.of(value);
        class_defs.add(container);
        add(value.getType());
        var interfaces = container.interfaces();
        if (interfaces != null) add(interfaces);
        add(container.interfaces());
        var superclass = value.getSuperclass();
        if (superclass != null) add(superclass);
        add(container.static_values());
        for (var field : container.static_fields()) {
            fill(field);
        }
        for (var field : container.instance_fields()) {
            fill(field);
        }
        for (var method : container.direct_methods()) {
            fill(method);
        }
        for (var method : container.virtual_methods()) {
            fill(method);
        }
        // TODO: AnnotdtionDirectory
    }

    public void fill(Dex value) {
        for (var tmp : value.getClasses()) {
            add(tmp);
        }
    }

    private void add(List<TypeId> value) {
        type_lists.put(value, null);
        for (var tmp : value) {
            add(tmp);
        }
    }

    private void add(EncodedArray value) {
        encoded_arrays.put(value, null);
        fill(value);
    }

    private void fill(FieldDefContainer value) {
        add(value.id());
    }

    private void fill(MethodDefContainer value) {
        add(value.id());
        //TODO: implementation
        //TODO: debug info
    }

    private void fill(EncodedArray value) {
        for (var tmp : value.getValue()) {
            fillEncodedValue(tmp);
        }
    }

    private void fill(AnnotationElement value) {
        add(value.getName());
        fillEncodedValue(value.getValue());
    }

    private void fill(CommonAnnotation value) {
        add(value.getType());
        for (var element : value.getElements()) {
            fill(element);
        }
    }

    private void fillEncodedValue(EncodedValue raw) {
        if (raw instanceof EncodedString value) add(value.getValue());
        if (raw instanceof EncodedType value) add(value.getValue());
        if (raw instanceof EncodedEnum value) add(value.getValue());
        if (raw instanceof EncodedField value) add(value.getValue());
        if (raw instanceof EncodedMethod value) add(value.getValue());
        if (raw instanceof EncodedMethodHandle value) add(value.getValue());
        if (raw instanceof EncodedMethodType value) add(value.getValue());
        if (raw instanceof EncodedAnnotation value) fill(value);
        if (raw instanceof EncodedArray value) fill(value);
    }
}
