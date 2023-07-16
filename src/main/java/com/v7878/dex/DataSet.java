package com.v7878.dex;

import com.v7878.dex.EncodedValue.ArrayValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Temporary object. Needed to read or write
public class DataSet extends DataFilter {

    private final Set<String> strings;
    private final Set<TypeId> types;
    private final Set<ProtoId> protos;
    private final Set<FieldId> fields;
    private final Set<MethodId> methods;
    private final Set<MethodHandleItem> method_handles;
    private final Set<CallSiteId> call_sites;

    private final List<ClassDef> class_defs;
    private final List<ClassData> class_data_items;

    private final Map<ClassDef, AnnotationsDirectory> annotations_directories;

    private final Set<TypeList> type_lists;
    private final Set<AnnotationItem> annotations;
    private final Set<AnnotationSet> annotation_sets;
    private final Set<AnnotationSetList> annotation_set_lists;
    private final Set<ArrayValue> array_values;
    private final Set<CodeItem> code_items;

    public DataSet() {
        strings = new HashSet<>();
        types = new HashSet<>();
        protos = new HashSet<>();
        fields = new HashSet<>();
        methods = new HashSet<>();
        method_handles = new HashSet<>();
        call_sites = new HashSet<>();

        class_defs = new ArrayList<>();
        class_data_items = new ArrayList<>();

        annotations_directories = new HashMap<>();

        type_lists = new HashSet<>();
        annotations = new HashSet<>();
        annotation_sets = new HashSet<>();
        annotation_set_lists = new HashSet<>();
        array_values = new HashSet<>();
        code_items = new HashSet<>();
    }

    @Override
    public void add(String value) {
        strings.add(value);
    }

    @Override
    public void add(TypeId value) {
        super.add(value);
        types.add(value);
    }

    @Override
    public void add(ProtoId value) {
        super.add(value);
        protos.add(value);
    }

    @Override
    public void add(FieldId value) {
        super.add(value);
        fields.add(value);
    }

    @Override
    public void add(MethodId value) {
        super.add(value);
        methods.add(value);
    }

    @Override
    public void add(MethodHandleItem value) {
        super.add(value);
        method_handles.add(value);
    }

    @Override
    public void add(CallSiteId value) {
        super.add(value);
        call_sites.add(value);
    }

    @Override
    public void add(ClassDef value) {
        super.add(value);
        class_defs.add(value);
    }

    @Override
    public void add(ClassData value) {
        if (value.isEmpty()) {
            throw new IllegalStateException("class_data is empty");
        }
        super.add(value);
        class_data_items.add(value);
    }

    @Override
    public void add(TypeList value) {
        super.add(value);
        type_lists.add(value);
    }

    @Override
    public void add(AnnotationItem value) {
        super.add(value);
        annotations.add(value);
    }

    @Override
    public void add(AnnotationSet value) {
        if (value.isEmpty()) {
            throw new IllegalStateException("annotation_set is empty");
        }
        super.add(value);
        annotation_sets.add(value);
    }

    @Override
    public void add(AnnotationSetList value) {
        if (value.isEmpty()) {
            throw new IllegalStateException("annotation_set_list is empty");
        }
        super.add(value);
        annotation_set_lists.add(value);
    }

    @Override
    public void add(ClassDef clazz, AnnotationsDirectory value) {
        if (annotations_directories.putIfAbsent(clazz, value) != null) {
            throw new IllegalStateException(
                    "annotations_directories contain duplicates");
        }
    }

    @Override
    public void add(ArrayValue value) {
        super.add(value);
        array_values.add(value);
    }

    @Override
    public void add(CodeItem value) {
        super.add(value);
        code_items.add(value);
    }

    public String[] getStrings() {
        return strings.toArray(new String[0]);
    }

    public TypeId[] getTypes() {
        return types.toArray(new TypeId[0]);
    }

    public ProtoId[] getProtos() {
        return protos.toArray(new ProtoId[0]);
    }

    public FieldId[] getFields() {
        return fields.toArray(new FieldId[0]);
    }

    public MethodId[] getMethods() {
        return methods.toArray(new MethodId[0]);
    }

    public MethodHandleItem[] getMethodHandles() {
        return method_handles.toArray(new MethodHandleItem[0]);
    }

    public CallSiteId[] getCallSites() {
        return call_sites.toArray(new CallSiteId[0]);
    }

    public ClassDef[] getClassDefs() {
        return ClassDef.sort(class_defs);
    }

    public ClassData[] getClassDataItems() {
        return class_data_items.toArray(new ClassData[0]);
    }

    public TypeList[] getTypeLists() {
        return type_lists.toArray(new TypeList[0]);
    }

    public AnnotationItem[] getAnnotations() {
        return annotations.toArray(new AnnotationItem[0]);
    }

    public AnnotationSet[] getAnnotationSets() {
        return annotation_sets.toArray(new AnnotationSet[0]);
    }

    public AnnotationSetList[] getAnnotationSetLists() {
        return annotation_set_lists.toArray(new AnnotationSetList[0]);
    }

    public Map<ClassDef, AnnotationsDirectory> getAnnotationsDirectories() {
        return annotations_directories;
    }

    public ArrayValue[] getArrayValues() {
        return array_values.toArray(new ArrayValue[0]);
    }

    public CodeItem[] getCodeItems() {
        return code_items.toArray(new CodeItem[0]);
    }
}
