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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

// Temporary object. Needed to write dex
final class WriteContextImpl implements WriteContext {

    private final String[] strings;
    private final TypeId[] types;
    private final ProtoId[] protos;
    private final FieldId[] fields;
    private final MethodId[] methods;
    private final ClassDef[] class_defs;
    private final CallSiteId[] call_sites;
    private final MethodHandleItem[] method_handles;
    private final Map<TypeList, Integer> type_lists;
    private final Map<AnnotationItem, Integer> annotations;
    private final Map<AnnotationSet, Integer> annotation_sets;
    private final Map<AnnotationSetList, Integer> annotation_set_lists;
    private final Map<ArrayValue, Integer> array_values;
    private final Map<ClassData, Integer> class_data_items;
    private final Map<TypeId, Integer> annotations_directories;
    private final Map<CodeItem, Integer> code_items;

    private final WriteOptions options;

    public WriteContextImpl(DataSet data, WriteOptions options) {
        this.options = options;

        strings = data.getStrings();
        Arrays.sort(strings, StringId.COMPARATOR);
        types = data.getTypes();
        Arrays.sort(types, TypeId.COMPARATOR);
        protos = data.getProtos();
        Arrays.sort(protos, ProtoId.COMPARATOR);
        fields = data.getFields();
        Arrays.sort(fields, FieldId.COMPARATOR);
        methods = data.getMethods();
        Arrays.sort(methods, MethodId.COMPARATOR);
        call_sites = data.getCallSites();
        Arrays.sort(call_sites, CallSiteId.COMPARATOR);
        method_handles = data.getMethodHandles();
        Arrays.sort(method_handles, MethodHandleItem.COMPARATOR);

        class_defs = data.getClassDefs();

        type_lists = new HashMap<>();
        annotations = new HashMap<>();
        annotation_sets = new HashMap<>();
        annotation_set_lists = new HashMap<>();
        class_data_items = new HashMap<>();
        annotations_directories = new HashMap<>();
        array_values = new HashMap<>();
        code_items = new HashMap<>();
    }

    @Override
    public WriteOptions getOptions() {
        return options;
    }

    public void addTypeList(TypeList value, int offset) {
        type_lists.put(value, offset);
    }

    public void addAnnotation(AnnotationItem value, int offset) {
        annotations.put(value, offset);
    }

    public void addAnnotationSet(AnnotationSet value, int offset) {
        annotation_sets.put(value, offset);
    }

    public void addAnnotationSetList(AnnotationSetList value, int offset) {
        annotation_set_lists.put(value, offset);
    }

    public void addClassData(ClassData value, int offset) {
        class_data_items.put(value, offset);
    }

    public void addAnnotationsDirectory(TypeId value, int offset) {
        annotations_directories.put(value, offset);
    }

    public void addArrayValue(ArrayValue value, int offset) {
        array_values.put(value, offset);
    }

    public void addCodeItem(CodeItem value, int offset) {
        code_items.put(value, offset);
    }

    public Stream<String> stringsStream() {
        return Arrays.stream(strings);
    }

    public Stream<TypeId> typesStream() {
        return Arrays.stream(types);
    }

    public Stream<ProtoId> protosStream() {
        return Arrays.stream(protos);
    }

    public Stream<FieldId> fieldsStream() {
        return Arrays.stream(fields);
    }

    public Stream<MethodId> methodsStream() {
        return Arrays.stream(methods);
    }

    public Stream<CallSiteId> callSitesStream() {
        return Arrays.stream(call_sites);
    }

    public Stream<MethodHandleItem> methodHandlesStream() {
        return Arrays.stream(method_handles);
    }

    public Stream<ClassDef> classDefsStream() {
        return Arrays.stream(class_defs);
    }

    public int getStringsCount() {
        return strings.length;
    }

    public int getTypesCount() {
        return types.length;
    }

    public int getProtosCount() {
        return protos.length;
    }

    public int getFieldsCount() {
        return fields.length;
    }

    public int getMethodsCount() {
        return methods.length;
    }

    public int getClassDefsCount() {
        return class_defs.length;
    }

    public int getCallSitesCount() {
        return call_sites.length;
    }

    public int getMethodHandlesCount() {
        return method_handles.length;
    }

    @Override
    public int getStringIndex(String value) {
        int out = Arrays.binarySearch(strings, value, StringId.COMPARATOR);
        if (out < 0) {
            throw new IllegalArgumentException(
                    "unable to find string \"" + value + "\"");
        }
        return out;
    }

    @Override
    public int getTypeIndex(TypeId value) {
        int out = Arrays.binarySearch(types, value, TypeId.COMPARATOR);
        if (out < 0) {
            throw new IllegalArgumentException(
                    "unable to find type \"" + value + "\"");
        }
        return out;
    }

    @Override
    public int getProtoIndex(ProtoId value) {
        int out = Arrays.binarySearch(protos, value, ProtoId.COMPARATOR);
        if (out < 0) {
            throw new IllegalArgumentException(
                    "unable to find proto \"" + value + "\"");
        }
        return out;
    }

    @Override
    public int getFieldIndex(FieldId value) {
        int out = Arrays.binarySearch(fields, value, FieldId.COMPARATOR);
        if (out < 0) {
            throw new IllegalArgumentException(
                    "unable to find field \"" + value + "\"");
        }
        return out;
    }

    @Override
    public int getMethodIndex(MethodId value) {
        int out = Arrays.binarySearch(methods, value, MethodId.COMPARATOR);
        if (out < 0) {
            throw new IllegalArgumentException(
                    "unable to find method \"" + value + "\"");
        }
        return out;
    }

    @Override
    public int getCallSiteIndex(CallSiteId value) {
        int out = Arrays.binarySearch(call_sites, value, CallSiteId.COMPARATOR);
        if (out < 0) {
            throw new IllegalArgumentException(
                    "unable to find method handle \"" + value + "\"");
        }
        return out;
    }

    @Override
    public int getMethodHandleIndex(MethodHandleItem value) {
        int out = Arrays.binarySearch(method_handles, value, MethodHandleItem.COMPARATOR);
        if (out < 0) {
            throw new IllegalArgumentException(
                    "unable to find method handle \"" + value + "\"");
        }
        return out;
    }

    @Override
    public int getTypeListOffset(TypeList value) {
        Integer out = type_lists.get(value);
        if (out == null) {
            throw new IllegalArgumentException(
                    "unable to type list \"" + value + "\"");
        }
        return out;
    }

    @Override
    public int getAnnotationOffset(AnnotationItem value) {
        Integer out = annotations.get(value);
        if (out == null) {
            throw new IllegalArgumentException(
                    "unable to find annotation \"" + value + "\"");
        }
        return out;
    }

    @Override
    public int getAnnotationSetOffset(AnnotationSet value) {
        Integer out = annotation_sets.get(value);
        if (out == null) {
            throw new IllegalArgumentException(
                    "unable to find annotation set \"" + value + "\"");
        }
        return out;
    }

    @Override
    public int getAnnotationSetListOffset(AnnotationSetList value) {
        Integer out = annotation_set_lists.get(value);
        if (out == null) {
            throw new IllegalArgumentException(
                    "unable to find annotation set list \"" + value + "\"");
        }
        return out;
    }

    @Override
    public int getClassDataOffset(ClassData value) {
        Integer out = class_data_items.get(value);
        if (out == null) {
            throw new IllegalArgumentException(
                    "unable to find class data \"" + value + "\"");
        }
        return out;
    }

    @Override
    public int getAnnotationsDirectoryOffset(TypeId value) {
        Integer out = annotations_directories.get(value);
        if (out == null) {
            throw new IllegalArgumentException(
                    "unable to find annotations directory for type \""
                            + value + "\"");
        }
        return out;
    }

    @Override
    public int getArrayValueOffset(ArrayValue value) {
        Integer out = array_values.get(value);
        if (out == null) {
            throw new IllegalArgumentException(
                    "unable to find array value \"" + value + "\"");
        }
        return out;
    }

    @Override
    public int getCodeItemOffset(CodeItem value) {
        Integer out = code_items.get(value);
        if (out == null) {
            throw new IllegalArgumentException(
                    "unable to find code item \"" + value + "\"");
        }
        return out;
    }
}
