/*
 * Copyright (c) 2023 Vladimir Kozelkov
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.v7878.dex;

import com.v7878.dex.EncodedValue.ArrayValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Temporary object. Needed to read or write
final class DataSet extends DataFilter {

    private final Set<String> strings;
    private final Set<TypeId> types;
    private final Set<ProtoId> protos;
    private final Set<FieldId> fields;
    private final Set<MethodId> methods;
    private final Set<MethodHandleItem> method_handles;
    private final Set<CallSiteId> call_sites;

    private final List<ClassDef> class_defs;
    private final List<ClassData> class_data_items;

    private final Map<TypeId, AnnotationsDirectory> annotations_directories;

    private final Set<TypeList> type_lists;
    private final Set<AnnotationItem> annotations;
    private final Set<AnnotationSet> annotation_sets;
    private final Set<AnnotationSetList> annotation_set_lists;
    private final Set<ArrayValue> array_values;
    private final Set<CodeItem> code_items;
    private final Set<DebugInfo> debug_infos;

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
        debug_infos = new HashSet<>();
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
        getAnnotationsDirectory(value.getType()).setClassAnnotations(value.getAnnotations());
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
    public void add(ArrayValue value) {
        super.add(value);
        array_values.add(value);
    }

    @Override
    public void add(CodeItem value) {
        super.add(value);
        code_items.add(value);
    }

    @Override
    public void add(DebugInfo value) {
        super.add(value);
        if (value.isEmpty()) {
            throw new IllegalStateException("debug_info is empty");
        }
        debug_infos.add(value);
    }

    @Override
    public void fill(EncodedField value) {
        super.fill(value);
        AnnotationSet fannotations = value.getAnnotations();
        if (!fannotations.isEmpty()) {
            FieldId id = value.getField();
            AnnotationsDirectory directory = getAnnotationsDirectory(id.getDeclaringClass());
            directory.addFieldAnnotations(id, fannotations);
        }
    }

    @Override
    public void fill(EncodedMethod value) {
        super.fill(value);
        MethodId id = value.getMethod();
        AnnotationsDirectory directory = getAnnotationsDirectory(id.getDeclaringClass());
        AnnotationSet mannotations = value.getAnnotations();
        if (!mannotations.isEmpty()) {
            directory.addMethodAnnotations(id, mannotations);
        }
        AnnotationSetList pannotations = value.getParameterAnnotations();
        if (!pannotations.isEmpty()) {
            directory.addMethodParameterAnnotations(id, pannotations);
        }
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

    private AnnotationsDirectory getAnnotationsDirectory(TypeId clazz) {
        AnnotationsDirectory out = annotations_directories.get(clazz);
        if (out == null) {
            annotations_directories.put(clazz, out = AnnotationsDirectory.empty());
        }
        return out;
    }

    public Map<TypeId, AnnotationsDirectory> getAnnotationsDirectories() {
        return annotations_directories;
    }

    public ArrayValue[] getArrayValues() {
        return array_values.toArray(new ArrayValue[0]);
    }

    public CodeItem[] getCodeItems() {
        return code_items.toArray(new CodeItem[0]);
    }

    public DebugInfo[] getDebugInfos() {
        return debug_infos.toArray(new DebugInfo[0]);
    }
}
