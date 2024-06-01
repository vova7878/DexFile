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
import com.v7878.dex.io.ByteArrayIO;
import com.v7878.dex.io.RandomIO;
import com.v7878.dex.io.RandomInput;
import com.v7878.dex.io.RandomOutput;
import com.v7878.dex.util.MutableList;
import com.v7878.misc.Checks;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public final class Dex extends MutableList<ClassDef> {

    public Dex(int initialCapacity) {
        super(initialCapacity);
    }

    public Dex(ClassDef... class_defs) {
        super(class_defs);
    }

    public Dex(Collection<ClassDef> class_defs) {
        super(class_defs);
    }

    @Override
    protected ClassDef check(ClassDef class_def) {
        return Objects.requireNonNull(class_def,
                "Dex can`t contain null class def");
    }

    public static Dex read(RandomInput src) {
        return read(src, null);
    }


    public static Dex read(RandomInput src, int[] class_def_ids) {
        return read(src, ReadOptions.defaultOptions(), class_def_ids);
    }

    public static Dex read(RandomInput src, ReadOptions options, int[] class_def_ids) {
        RandomInput in = src.position() == 0 ? src : src.slice();

        FileMap map = FileMap.read(in, options);

        if (class_def_ids == null) {
            class_def_ids = new int[map.class_defs_size];
            for (int i = 0; i < map.class_defs_size; i++) {
                class_def_ids[i] = i;
            }
        } else {
            //TODO: check unique
            for (int id : class_def_ids) {
                Checks.checkIndex(id, map.class_defs_size);
            }
        }

        if (class_def_ids.length == 0) {
            return new Dex();
        }

        return readInternal(in, options, class_def_ids, map);
    }

    private static Dex readInternal(RandomInput in, ReadOptions options,
                                    int[] class_def_ids, FileMap map) {

        ReadContextImpl context = new ReadContextImpl(options);

        context.setStrings(StringId.readArray(in.duplicate(map.string_ids_off), map.string_ids_size));
        context.setTypes(TypeId.readArray(in.duplicate(map.type_ids_off), context, map.type_ids_size));
        context.setProtos(ProtoId.readArray(in.duplicate(map.proto_ids_off), context, map.proto_ids_size));
        context.setFields(FieldId.readArray(in.duplicate(map.field_ids_off), context, map.field_ids_size));
        context.setMethods(MethodId.readArray(in.duplicate(map.method_ids_off), context, map.method_ids_size));
        context.setMethodHandles(MethodHandleItem.readArray(in.duplicate(map.method_handles_off), context, map.method_handles_size));
        context.setCallSites(CallSiteId.readArray(in.duplicate(map.call_site_ids_off), context, map.call_site_ids_size));

        // TODO: move to ClassDef.readArray?
        ClassDef[] class_defs = new ClassDef[class_def_ids.length];
        for (int i = 0; i < class_def_ids.length; i++) {
            int offset = map.class_defs_off + ClassDef.SIZE * class_def_ids[i];
            RandomInput in2 = in.duplicate(offset);
            class_defs[i] = ClassDef.read(in2, context);
        }

        return new Dex(class_defs);
    }

    public void collectData(DataCollector data) {
        for (ClassDef tmp : this) {
            data.add(tmp);
        }
    }

    public void write(RandomIO dst) {
        write(dst, WriteOptions.defaultOptions());
    }

    public void write(RandomIO dst, WriteOptions options) {
        RandomIO out = dst.position() == 0 ? dst : dst.slice();
        writeInternal(out, options);
    }

    //TODO: all sections
    private void writeInternal(RandomIO out, WriteOptions options) {
        DataSet sections = new DataSet();
        collectData(sections);

        WriteContextImpl context = new WriteContextImpl(sections, options);

        FileMap map = new FileMap();

        RandomOutput data;

        {
            int offset = FileMap.HEADER_SIZE;

            map.string_ids_off = offset;
            map.string_ids_size = context.getStringsCount();
            offset += map.string_ids_size * StringId.SIZE;

            map.type_ids_off = offset;
            map.type_ids_size = context.getTypesCount();
            offset += map.type_ids_size * TypeId.SIZE;

            map.proto_ids_off = offset;
            map.proto_ids_size = context.getProtosCount();
            offset += map.proto_ids_size * ProtoId.SIZE;

            map.field_ids_off = offset;
            map.field_ids_size = context.getFieldsCount();
            offset += map.field_ids_size * FieldId.SIZE;

            map.method_ids_off = offset;
            map.method_ids_size = context.getMethodsCount();
            offset += map.method_ids_size * MethodId.SIZE;

            map.class_defs_off = offset;
            map.class_defs_size = context.getClassDefsCount();
            offset += map.class_defs_size * ClassDef.SIZE;

            map.call_site_ids_off = offset;
            map.call_site_ids_size = context.getCallSitesCount();
            offset += map.call_site_ids_size * CallSiteId.SIZE;

            map.method_handles_off = offset;
            map.method_handles_size = context.getMethodHandlesCount();
            offset += map.method_handles_size * MethodHandleItem.SIZE;

            map.data_off = offset;

            data = out.duplicate(offset);
        }

        map.string_data_items_off = (int) data.position();
        map.string_data_items_size = map.string_ids_size;
        out.position(map.string_ids_off);
        context.stringsStream().forEachOrdered(value -> StringId.write(value, context, out, data));

        TypeList[] lists = sections.getTypeLists();
        if (lists.length != 0) {
            data.alignPosition(TypeList.ALIGNMENT);
            map.type_lists_off = (int) data.position();
            map.type_lists_size = lists.length;
            for (TypeList tmp : lists) {
                data.alignPosition(TypeList.ALIGNMENT);
                int start = (int) data.position();
                tmp.write(context, data);
                context.addTypeList(tmp, start);
            }
        }

        AnnotationItem[] annotations = sections.getAnnotations();
        map.annotations_off = (int) data.position();
        map.annotations_size = annotations.length;
        for (AnnotationItem tmp : annotations) {
            int start = (int) data.position();
            tmp.write(context, data);
            context.addAnnotation(tmp, start);
        }

        AnnotationSet[] annotation_sets = sections.getAnnotationSets();
        if (annotation_sets.length != 0) {
            data.alignPosition(AnnotationSet.ALIGNMENT);
            map.annotation_sets_off = (int) data.position();
            map.annotation_sets_size = annotation_sets.length;
            for (AnnotationSet tmp : annotation_sets) {
                int start = (int) data.position();
                tmp.write(context, data);
                context.addAnnotationSet(tmp, start);
            }
        }

        AnnotationSetList[] annotation_set_lists = sections.getAnnotationSetLists();
        if (annotation_set_lists.length != 0) {
            data.alignPosition(AnnotationSetList.ALIGNMENT);
            map.annotation_set_refs_off = (int) data.position();
            map.annotation_set_refs_size = annotation_set_lists.length;
            for (AnnotationSetList tmp : annotation_set_lists) {
                int start = (int) data.position();
                tmp.write(context, data);
                context.addAnnotationSetList(tmp, start);
            }
        }

        CodeItem[] code_items = sections.getCodeItems();
        if (code_items.length != 0) {
            data.alignPosition(CodeItem.ALIGNMENT);
            map.code_items_off = (int) data.position();
            map.code_items_size = code_items.length;
            for (CodeItem tmp : code_items) {
                data.alignPosition(CodeItem.ALIGNMENT);
                int start = (int) data.position();
                tmp.write(context, data);
                context.addCodeItem(tmp, start);
            }
        }

        ClassData[] class_data_items = sections.getClassDataItems();
        map.class_data_items_off = (int) data.position();
        map.class_data_items_size = class_data_items.length;
        for (ClassData tmp : class_data_items) {
            int start = (int) data.position();
            tmp.write(context, data);
            context.addClassData(tmp, start);
        }

        //TODO: delete duplicates
        Map<ClassDef, AnnotationsDirectory> annotations_directories
                = sections.getAnnotationsDirectories();
        if (!annotations_directories.isEmpty()) {
            data.alignPosition(AnnotationsDirectory.ALIGNMENT);
            map.annotations_directories_off = (int) data.position();
            map.annotations_directories_size = 0;
            for (Map.Entry<ClassDef, AnnotationsDirectory> tmp
                    : annotations_directories.entrySet()) {
                AnnotationsDirectory ad = tmp.getValue();
                if (!ad.isEmpty()) {
                    map.annotations_directories_size++;
                    data.alignPosition(AnnotationsDirectory.ALIGNMENT);
                    int start = (int) data.position();
                    ad.write(context, data);
                    context.addAnnotationsDirectory(tmp.getKey(), start);
                } else {
                    context.addAnnotationsDirectory(tmp.getKey(), 0);
                }
            }
        }

        ArrayValue[] array_values = sections.getArrayValues();
        map.encoded_arrays_off = (int) data.position();
        map.encoded_arrays_size = array_values.length;
        for (ArrayValue tmp : array_values) {
            int start = (int) data.position();
            tmp.writeData(context, data);
            context.addArrayValue(tmp, start);
        }

        data.alignPosition(FileMap.MAP_ALIGNMENT);
        map.writeMap(data);

        int file_size = (int) data.position();

        map.data_size = file_size - map.data_off;

        out.position(map.type_ids_off);
        context.typesStream().forEachOrdered((value) -> value.write(context, out));

        out.position(map.field_ids_off);
        context.fieldsStream().forEachOrdered((value) -> value.write(context, out));

        out.position(map.proto_ids_off);
        context.protosStream().forEachOrdered((value) -> value.write(context, out));

        out.position(map.method_ids_off);
        context.methodsStream().forEachOrdered((value) -> value.write(context, out));

        out.position(map.class_defs_off);
        context.classDefsStream().forEachOrdered((value) -> value.write(context, out));

        out.position(map.call_site_ids_off);
        context.callSitesStream().forEachOrdered((value) -> value.write(context, out));

        out.position(map.method_handles_off);
        context.methodHandlesStream().forEachOrdered((value) -> value.write(context, out));

        map.writeHeader(out, options, file_size);
    }

    public ClassDef findClassDef(TypeId type) {
        for (ClassDef tmp : this) {
            if (tmp.getType().equals(type)) {
                return tmp;
            }
        }
        return null;
    }

    public byte[] compile(WriteOptions options) {
        ByteArrayIO out = new ByteArrayIO();
        write(out, options);
        return out.toByteArray();
    }

    public byte[] compile() {
        return compile(WriteOptions.defaultOptions());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Dex && super.equals(obj);
    }

    // hashcode from super

    @Override
    public Dex mutate() {
        return new Dex(this);
    }
}
